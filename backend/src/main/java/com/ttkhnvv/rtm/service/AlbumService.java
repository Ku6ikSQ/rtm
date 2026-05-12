package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.album.AlbumArtistSummary;
import com.ttkhnvv.rtm.dto.pagination.PageResponse;
import com.ttkhnvv.rtm.dto.album.AlbumFilter;
import com.ttkhnvv.rtm.dto.album.AlbumResponse;
import com.ttkhnvv.rtm.dto.album.CreateAlbumRequest;
import com.ttkhnvv.rtm.entity.album.Album;
import com.ttkhnvv.rtm.entity.albumartist.AlbumArtist;
import com.ttkhnvv.rtm.entity.artist.Artist;
import com.ttkhnvv.rtm.exception.album.AlbumNotFoundException;
import com.ttkhnvv.rtm.mapper.AlbumMapper;
import com.ttkhnvv.rtm.repository.album.AlbumRepository;
import com.ttkhnvv.rtm.repository.album.AlbumSpecs;
import com.ttkhnvv.rtm.repository.albumartist.AlbumArtistRepository;
import com.ttkhnvv.rtm.repository.artist.ArtistRepository;
import com.ttkhnvv.rtm.repository.review.ReviewRepository;
import com.ttkhnvv.rtm.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages album catalog operations including filtering, cover image storage and average rating maintenance.
 * Cover images are stored in object storage; presigned URLs are generated on read.
 * Artists and review counts are fetched in batch to avoid N+1 queries.
 */
@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final ReviewRepository reviewRepository;
    private final AlbumArtistRepository albumArtistRepository;
    private final ArtistRepository artistRepository;
    private final AlbumMapper albumMapper;
    private final StorageService storageService;

    /**
     * Returns a paginated list of albums matching the given filter criteria.
     * Cover URLs in the response are presigned and valid for 60 minutes.
     * Artists and review counts are fetched in batch queries — no N+1.
     *
     * @param filter   optional filters (title substring, release year, genre id)
     * @param pageable pagination and sorting parameters
     * @return page of album responses
     */
    @Transactional(readOnly = true)
    public PageResponse<AlbumResponse> getAll(AlbumFilter filter, Pageable pageable) {
        var spec = AlbumSpecs.titleOrArtistContains(filter.getTitle())
                .and(AlbumSpecs.releaseYearFrom(filter.getYearFrom()))
                .and(AlbumSpecs.releaseYearTo(filter.getYearTo()))
                .and(AlbumSpecs.ratingMin(filter.getRatingMin()))
                .and(AlbumSpecs.ratingMax(filter.getRatingMax()))
                .and(AlbumSpecs.byGenreId(filter.getGenreId()))
                .and(AlbumSpecs.byArtistId(filter.getArtistId()));
        var page = albumRepository.findAll(spec, pageable);

        var albumIds = page.getContent().stream().map(Album::getId).toList();
        var artistsByAlbum = fetchArtistsByAlbum(albumIds);
        var reviewCountByAlbum = fetchReviewCountByAlbum(albumIds);

        var content = page.getContent().stream()
                .map(album -> toResponseWithDetails(
                        album,
                        artistsByAlbum.getOrDefault(album.getId(), List.of()),
                        reviewCountByAlbum.getOrDefault(album.getId(), 0L)))
                .toList();
        return PageResponse.of(page, content);
    }

    /**
     * Returns a single album by its identifier.
     * The cover URL in the response is presigned and valid for 60 minutes.
     * Artists and review count are included in the response.
     *
     * @param id album identifier
     * @return album response with a presigned cover URL, artists and review count
     * @throws AlbumNotFoundException if no album was found with the given id
     */
    @Transactional(readOnly = true)
    public AlbumResponse getById(UUID id) {
        var album = findAlbumById(id);
        var albumArtists = albumArtistRepository.findAllByAlbumId(id);
        var artistIds = albumArtists.stream().map(AlbumArtist::getArtistId).distinct().toList();
        var artistMap = artistRepository.findAllById(artistIds).stream()
                .collect(Collectors.toMap(Artist::getId, a -> a));
        var artists = albumArtists.stream()
                .map(aa -> toSummary(aa, artistMap))
                .toList();
        var reviewCount = reviewRepository.countByAlbumId(id);
        return toResponseWithDetails(album, artists, reviewCount);
    }

    /**
     * Creates a new album and records the creating user.
     *
     * @param request   album data (title, description, release year)
     * @param createdBy identifier of the user creating the album
     * @return the created album response
     */
    @Transactional
    public AlbumResponse create(CreateAlbumRequest request, UUID createdBy) {
        var album = Album.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .releaseYear(request.getReleaseYear())
                .createdBy(createdBy)
                .build();
        return albumMapper.toResponse(albumRepository.save(album));
    }

    /**
     * Updates the title of an album.
     *
     * @param id    album identifier
     * @param title new title
     * @throws AlbumNotFoundException if no album was found with the given id
     */
    @Transactional
    public void updateTitle(UUID id, String title) {
        var album = findAlbumById(id);
        album.setTitle(title);
        albumRepository.save(album);
    }

    /**
     * Updates the description of an album.
     *
     * @param id          album identifier
     * @param description new description
     * @throws AlbumNotFoundException if no album was found with the given id
     */
    @Transactional
    public void updateDescription(UUID id, String description) {
        var album = findAlbumById(id);
        album.setDescription(description);
        albumRepository.save(album);
    }

    /**
     * Updates the release year of an album.
     *
     * @param id          album identifier
     * @param releaseYear new release year
     * @throws AlbumNotFoundException if no album was found with the given id
     */
    @Transactional
    public void updateReleaseYear(UUID id, Integer releaseYear) {
        var album = findAlbumById(id);
        album.setReleaseYear(releaseYear);
        albumRepository.save(album);
    }

    /**
     * Replaces the cover image of an album. Deletes the previous cover from storage if one exists.
     *
     * @param id   album identifier
     * @param file new cover image (jpeg, png or webp)
     * @throws AlbumNotFoundException if no album was found with the given id
     */
    @Transactional
    public void updateCover(UUID id, MultipartFile file) {
        var album = findAlbumById(id);
        if (album.getCoverKey() != null)
            storageService.delete(album.getCoverKey());
        album.setCoverKey(storageService.upload(file));
        albumRepository.save(album);
    }

    /**
     * Deletes an album and its cover image from storage if one exists.
     *
     * @param id album identifier
     * @throws AlbumNotFoundException if no album was found with the given id
     */
    @Transactional
    public void delete(UUID id) {
        var album = findAlbumById(id);
        if (album.getCoverKey() != null)
            storageService.delete(album.getCoverKey());
        albumRepository.deleteById(id);
    }

    /**
     * Recalculates and persists the average review score for an album.
     * Called automatically after a review is created, updated or deleted.
     *
     * @param id album identifier
     * @throws AlbumNotFoundException if no album was found with the given id
     */
    @Transactional
    public void recalculateRating(UUID id) {
        var album = findAlbumById(id);
        album.setAvgRating(reviewRepository.calculateAvgRating(id).orElse(null));
        albumRepository.save(album);
    }

    private AlbumResponse toResponseWithDetails(Album album, List<AlbumArtistSummary> artists, long reviewCount) {
        var response = albumMapper.toResponse(album);
        if (album.getCoverKey() != null)
            response.setCoverUrl(storageService.getPresignedUrl(album.getCoverKey()));
        response.setArtists(artists);
        response.setReviewCount(reviewCount);
        return response;
    }

    private Map<UUID, List<AlbumArtistSummary>> fetchArtistsByAlbum(List<UUID> albumIds) {
        if (albumIds.isEmpty()) return Map.of();
        var albumArtists = albumArtistRepository.findAllByAlbumIdIn(albumIds);
        var artistIds = albumArtists.stream().map(AlbumArtist::getArtistId).distinct().toList();
        var artistMap = artistRepository.findAllById(artistIds).stream()
                .collect(Collectors.toMap(Artist::getId, a -> a));
        return albumArtists.stream()
                .collect(Collectors.groupingBy(
                        AlbumArtist::getAlbumId,
                        Collectors.mapping(aa -> toSummary(aa, artistMap), Collectors.toList())));
    }

    private Map<UUID, Long> fetchReviewCountByAlbum(List<UUID> albumIds) {
        if (albumIds.isEmpty()) return Map.of();
        return reviewRepository.countGroupedByAlbumIdIn(albumIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]));
    }

    private AlbumArtistSummary toSummary(AlbumArtist aa, Map<UUID, Artist> artistMap) {
        var artist = artistMap.get(aa.getArtistId());
        return new AlbumArtistSummary(
                aa.getAlbumId(),
                aa.getArtistId(),
                artist != null ? artist.getStageName() : null,
                aa.getRole(),
                aa.getOrder());
    }

    private Album findAlbumById(UUID id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Failed to find album."));
    }
}