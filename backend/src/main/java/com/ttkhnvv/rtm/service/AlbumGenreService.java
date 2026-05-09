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

@Service
@RequiredArgsConstructor
public class AlbumGenreService {
    private final AlbumGenreRepository albumGenreRepository;
    private final AlbumGenreMapper albumGenreMapper;

    @Transactional(readOnly = true)
    public List<AlbumGenreResponse> getByAlbumId(UUID albumId) {
        return albumGenreRepository.findAllByAlbumId(albumId).stream()
                .map(albumGenreMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlbumGenreResponse getById(UUID albumId, UUID genreId) {
        return albumGenreMapper.toResponse(findById(albumId, genreId));
    }

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
