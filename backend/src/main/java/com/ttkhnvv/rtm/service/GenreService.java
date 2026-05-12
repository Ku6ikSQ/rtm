package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.pagination.PageResponse;
import com.ttkhnvv.rtm.dto.genre.CreateGenreRequest;
import com.ttkhnvv.rtm.dto.genre.GenreFilter;
import com.ttkhnvv.rtm.dto.genre.GenreResponse;
import com.ttkhnvv.rtm.entity.genre.Genre;
import com.ttkhnvv.rtm.exception.genre.GenreNotFoundException;
import com.ttkhnvv.rtm.exception.genre.GenreSlugAlreadyTakenException;
import com.ttkhnvv.rtm.mapper.GenreMapper;
import com.ttkhnvv.rtm.repository.albumgenre.AlbumGenreRepository;
import com.ttkhnvv.rtm.repository.genre.GenreRepository;
import com.ttkhnvv.rtm.repository.genre.GenreSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages the music genre catalog with hierarchical parent-child structure and unique slug enforcement.
 * Supports filtering by name substring and parent genre.
 * Album counts are fetched in batch to avoid N+1 queries.
 */
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;
    private final AlbumGenreRepository albumGenreRepository;
    private final GenreMapper genreMapper;

    /**
     * Returns a paginated list of genres matching the given filter criteria.
     * Album count per genre is included via a batch query — no N+1.
     *
     * @param filter   optional filters (name substring, parent genre id)
     * @param pageable pagination and sorting parameters
     * @return page of genre responses
     */
    @Transactional(readOnly = true)
    public PageResponse<GenreResponse> getAll(GenreFilter filter, Pageable pageable) {
        var spec = GenreSpecs.nameContains(filter.getName())
                .and(GenreSpecs.parentIdEquals(filter.getParentId()));
        var page = genreRepository.findAll(spec, pageable);

        var genreIds = page.getContent().stream().map(Genre::getId).toList();
        var countByGenre = fetchAlbumCountByGenre(genreIds);

        var content = page.getContent().stream()
                .map(genre -> toResponseWithCount(genre, countByGenre.getOrDefault(genre.getId(), 0L)))
                .toList();
        return PageResponse.of(page, content);
    }

    /**
     * Returns a single genre by its identifier, including album count.
     *
     * @param id genre identifier
     * @return genre response with album count
     * @throws GenreNotFoundException if no genre was found with the given id
     */
    @Transactional(readOnly = true)
    public GenreResponse getById(UUID id) {
        var genre = findGenreById(id);
        return toResponseWithCount(genre, albumGenreRepository.countByGenreId(id));
    }

    /**
     * Creates a new genre with a globally unique slug.
     *
     * @param request genre data (name, slug, description, optional parent id)
     * @return the created genre response
     * @throws GenreSlugAlreadyTakenException if the slug is already used by another genre
     */
    @Transactional
    public GenreResponse create(CreateGenreRequest request) {
        if (genreRepository.existsBySlug(request.getSlug()))
            throw new GenreSlugAlreadyTakenException("Slug is already taken.");
        var genre = Genre.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .build();
        return genreMapper.toResponse(genreRepository.save(genre));
    }

    /**
     * Updates the display name of a genre.
     *
     * @param id   genre identifier
     * @param name new display name
     * @throws GenreNotFoundException if no genre was found with the given id
     */
    @Transactional
    public void updateName(UUID id, String name) {
        var genre = findGenreById(id);
        genre.setName(name);
        genreRepository.save(genre);
    }

    /**
     * Updates the slug of a genre, enforcing uniqueness across all other genres.
     *
     * @param id   genre identifier
     * @param slug new slug value
     * @throws GenreSlugAlreadyTakenException if the slug is already used by another genre
     * @throws GenreNotFoundException         if no genre was found with the given id
     */
    @Transactional
    public void updateSlug(UUID id, String slug) {
        if (genreRepository.existsBySlugAndIdNot(slug, id))
            throw new GenreSlugAlreadyTakenException("Slug is already taken.");
        var genre = findGenreById(id);
        genre.setSlug(slug);
        genreRepository.save(genre);
    }

    /**
     * Updates the description of a genre.
     *
     * @param id          genre identifier
     * @param description new description text
     * @throws GenreNotFoundException if no genre was found with the given id
     */
    @Transactional
    public void updateDescription(UUID id, String description) {
        var genre = findGenreById(id);
        genre.setDescription(description);
        genreRepository.save(genre);
    }

    /**
     * Updates the parent genre, changing the genre's position in the hierarchy.
     *
     * @param id       genre identifier
     * @param parentId new parent genre identifier
     * @throws GenreNotFoundException if no genre was found with the given id
     */
    @Transactional
    public void updateParent(UUID id, UUID parentId) {
        var genre = findGenreById(id);
        genre.setParentId(parentId);
        genreRepository.save(genre);
    }

    /**
     * Deletes a genre by its identifier.
     *
     * @param id genre identifier
     * @throws GenreNotFoundException if no genre was found with the given id
     */
    @Transactional
    public void delete(UUID id) {
        findGenreById(id);
        genreRepository.deleteById(id);
    }

    private GenreResponse toResponseWithCount(Genre genre, long albumCount) {
        var response = genreMapper.toResponse(genre);
        response.setAlbumCount(albumCount);
        return response;
    }

    private Map<UUID, Long> fetchAlbumCountByGenre(List<UUID> genreIds) {
        if (genreIds.isEmpty()) return Map.of();
        return albumGenreRepository.countGroupedByGenreIdIn(genreIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]));
    }

    private Genre findGenreById(UUID id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new GenreNotFoundException("Failed to find genre."));
    }
}