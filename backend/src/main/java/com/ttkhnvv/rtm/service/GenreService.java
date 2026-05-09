package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.genre.CreateGenreRequest;
import com.ttkhnvv.rtm.dto.genre.GenreResponse;
import com.ttkhnvv.rtm.entity.genre.Genre;
import com.ttkhnvv.rtm.exception.genre.GenreNotFoundException;
import com.ttkhnvv.rtm.exception.genre.GenreSlugAlreadyTakenException;
import com.ttkhnvv.rtm.mapper.GenreMapper;
import com.ttkhnvv.rtm.repository.genre.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Transactional(readOnly = true)
    public List<GenreResponse> getAll() {
        return genreRepository.findAll().stream()
                .map(genreMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GenreResponse getById(UUID id) {
        return genreMapper.toResponse(findGenreById(id));
    }

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

    @Transactional
    public void updateName(UUID id, String name) {
        var genre = findGenreById(id);
        genre.setName(name);
        genreRepository.save(genre);
    }

    @Transactional
    public void updateSlug(UUID id, String slug) {
        if (genreRepository.existsBySlugAndIdNot(slug, id))
            throw new GenreSlugAlreadyTakenException("Slug is already taken.");
        var genre = findGenreById(id);
        genre.setSlug(slug);
        genreRepository.save(genre);
    }

    @Transactional
    public void updateDescription(UUID id, String description) {
        var genre = findGenreById(id);
        genre.setDescription(description);
        genreRepository.save(genre);
    }

    @Transactional
    public void updateParent(UUID id, UUID parentId) {
        var genre = findGenreById(id);
        genre.setParentId(parentId);
        genreRepository.save(genre);
    }

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
