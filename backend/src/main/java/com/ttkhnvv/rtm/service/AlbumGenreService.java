package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.albumgenre.AlbumGenreResponse;
import com.ttkhnvv.rtm.dto.albumgenre.CreateAlbumGenreRequest;
import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenre;
import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenreId;
import com.ttkhnvv.rtm.exception.albumgenre.AlbumGenreAlreadyExistsException;
import com.ttkhnvv.rtm.exception.albumgenre.AlbumGenreNotFoundException;
import com.ttkhnvv.rtm.mapper.AlbumGenreMapper;
import com.ttkhnvv.rtm.repository.albumgenre.AlbumGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Manages the many-to-many relationship between albums and genres.
 * Handles linking and composite-key re-keying operations.
 */
@Service
@RequiredArgsConstructor
public class AlbumGenreService {
    private final AlbumGenreRepository albumGenreRepository;
    private final AlbumGenreMapper albumGenreMapper;

    /**
     * Returns all genre links for the given album.
     *
     * @param albumId target album identifier
     * @return list of album-genre associations for the album
     */
    @Transactional(readOnly = true)
    public List<AlbumGenreResponse> getByAlbumId(UUID albumId) {
        return albumGenreRepository.findAllByAlbumId(albumId).stream()
                .map(albumGenreMapper::toResponse)
                .toList();
    }

    /**
     * Returns a single album-genre link by its composite key.
     *
     * @param albumId album identifier
     * @param genreId genre identifier
     * @return the album-genre association
     * @throws AlbumGenreNotFoundException if no link exists for the given pair
     */
    @Transactional(readOnly = true)
    public AlbumGenreResponse getById(UUID albumId, UUID genreId) {
        return albumGenreMapper.toResponse(findById(albumId, genreId));
    }

    /**
     * Creates a new link between an album and a genre.
     *
     * @param request link data (albumId and genreId)
     * @return the created album-genre association
     * @throws AlbumGenreAlreadyExistsException if the genre is already linked to the album
     */
    @Transactional
    public AlbumGenreResponse create(CreateAlbumGenreRequest request) {
        var id = new AlbumGenreId(request.getAlbumId(), request.getGenreId());
        if (albumGenreRepository.existsById(id))
            throw new AlbumGenreAlreadyExistsException("This genre is already linked to the album.");
        var albumGenre = AlbumGenre.builder()
                .albumId(request.getAlbumId())
                .genreId(request.getGenreId())
                .build();
        return albumGenreMapper.toResponse(albumGenreRepository.save(albumGenre));
    }

    /**
     * Moves the album-genre link to a different album by re-creating the record with the new composite key.
     *
     * @param albumId    current album identifier
     * @param genreId    genre identifier
     * @param newAlbumId target album identifier
     * @throws AlbumGenreNotFoundException      if the current link does not exist
     * @throws AlbumGenreAlreadyExistsException if the genre is already linked to the target album
     */
    @Transactional
    public void updateAlbumId(UUID albumId, UUID genreId, UUID newAlbumId) {
        var current = findById(albumId, genreId);
        var newId = new AlbumGenreId(newAlbumId, genreId);
        if (albumGenreRepository.existsById(newId))
            throw new AlbumGenreAlreadyExistsException("This genre is already linked to the target album.");
        albumGenreRepository.deleteById(new AlbumGenreId(albumId, genreId));
        albumGenreRepository.save(AlbumGenre.builder()
                .albumId(newAlbumId)
                .genreId(current.getGenreId())
                .build());
    }

    /**
     * Replaces the genre in an album-genre link by re-creating the record with the new composite key.
     *
     * @param albumId    album identifier
     * @param genreId    current genre identifier
     * @param newGenreId target genre identifier
     * @throws AlbumGenreNotFoundException      if the current link does not exist
     * @throws AlbumGenreAlreadyExistsException if the target genre is already linked to this album
     */
    @Transactional
    public void updateGenreId(UUID albumId, UUID genreId, UUID newGenreId) {
        var current = findById(albumId, genreId);
        var newId = new AlbumGenreId(albumId, newGenreId);
        if (albumGenreRepository.existsById(newId))
            throw new AlbumGenreAlreadyExistsException("The target genre is already linked to this album.");
        albumGenreRepository.deleteById(new AlbumGenreId(albumId, genreId));
        albumGenreRepository.save(AlbumGenre.builder()
                .albumId(current.getAlbumId())
                .genreId(newGenreId)
                .build());
    }

    /**
     * Removes the link between an album and a genre.
     *
     * @param albumId album identifier
     * @param genreId genre identifier
     * @throws AlbumGenreNotFoundException if the link does not exist
     */
    @Transactional
    public void delete(UUID albumId, UUID genreId) {
        findById(albumId, genreId);
        albumGenreRepository.deleteById(new AlbumGenreId(albumId, genreId));
    }

    private AlbumGenre findById(UUID albumId, UUID genreId) {
        return albumGenreRepository.findById(new AlbumGenreId(albumId, genreId))
                .orElseThrow(() -> new AlbumGenreNotFoundException("Failed to find album genre."));
    }
}
