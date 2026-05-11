package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.pagination.PageResponse;
import com.ttkhnvv.rtm.dto.genre.CreateGenreRequest;
import com.ttkhnvv.rtm.dto.genre.GenreFilter;
import com.ttkhnvv.rtm.dto.genre.GenreResponse;
import com.ttkhnvv.rtm.entity.genre.Genre;
import com.ttkhnvv.rtm.exception.genre.GenreNotFoundException;
import com.ttkhnvv.rtm.exception.genre.GenreSlugAlreadyTakenException;
import com.ttkhnvv.rtm.mapper.GenreMapper;
import com.ttkhnvv.rtm.repository.genre.GenreRepository;
import com.ttkhnvv.rtm.repository.genre.GenreSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Manages the music genre catalog with hierarchical parent-child structure and unique slug enforcement.
 * Supports filtering by name substring and parent genre.
 */
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    /**
     * Returns a paginated list of genres matching the given filter criteria.
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
        var content = page.getContent().stream()
                .map(genreMapper::toResponse)
                .toList();
        return PageResponse.of(page, content);
    }

    /**
     * Returns a single genre by its identifier.
     *
     * @param id genre identifier
     * @return genre response
     * @throws GenreNotFoundException if no genre was found with the given id
     */
    @Transactional(readOnly = true)
    public GenreResponse getById(UUID id) {
        return genreMapper.toResponse(findGenreById(id));
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

    private Genre findGenreById(UUID id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new GenreNotFoundException("Failed to find genre."));
    }
}
