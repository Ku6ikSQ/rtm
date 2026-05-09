package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.albumartist.AlbumArtistResponse;
import com.ttkhnvv.rtm.dto.albumartist.CreateAlbumArtistRequest;
import com.ttkhnvv.rtm.entity.albumartist.AlbumArtist;
import com.ttkhnvv.rtm.entity.albumartist.AlbumArtistId;
import com.ttkhnvv.rtm.entity.albumartist.ArtistRole;
import com.ttkhnvv.rtm.exception.albumartist.AlbumArtistAlreadyExistsException;
import com.ttkhnvv.rtm.exception.albumartist.AlbumArtistNotFoundException;
import com.ttkhnvv.rtm.mapper.AlbumArtistMapper;
import com.ttkhnvv.rtm.repository.albumartist.AlbumArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumArtistService {
    private final AlbumArtistRepository albumArtistRepository;
    private final AlbumArtistMapper albumArtistMapper;

    @Transactional(readOnly = true)
    public List<AlbumArtistResponse> getByAlbumId(UUID albumId) {
        return albumArtistRepository.findAllByAlbumId(albumId).stream()
                .map(albumArtistMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlbumArtistResponse getById(UUID albumId, UUID artistId) {
        return albumArtistMapper.toResponse(findById(albumId, artistId));
    }

    @Transactional
    public AlbumArtistResponse create(CreateAlbumArtistRequest request) {
        var id = new AlbumArtistId(request.getAlbumId(), request.getArtistId());
        if (albumArtistRepository.existsById(id))
            throw new AlbumArtistAlreadyExistsException("This artist is already linked to the album.");
        var albumArtist = AlbumArtist.builder()
                .albumId(request.getAlbumId())
                .artistId(request.getArtistId())
                .role(request.getRole())
                .order(request.getOrder())
                .build();
        return albumArtistMapper.toResponse(albumArtistRepository.save(albumArtist));
    }

    @Transactional
    public void updateAlbumId(UUID albumId, UUID artistId, UUID newAlbumId) {
        var current = findById(albumId, artistId);
        var newId = new AlbumArtistId(newAlbumId, artistId);
        if (albumArtistRepository.existsById(newId))
            throw new AlbumArtistAlreadyExistsException("This artist is already linked to the target album.");
        albumArtistRepository.deleteById(new AlbumArtistId(albumId, artistId));
        albumArtistRepository.save(AlbumArtist.builder()
                .albumId(newAlbumId)
                .artistId(artistId)
                .role(current.getRole())
                .order(current.getOrder())
                .build());
    }

    @Transactional
    public void updateArtistId(UUID albumId, UUID artistId, UUID newArtistId) {
        var current = findById(albumId, artistId);
        var newId = new AlbumArtistId(albumId, newArtistId);
        if (albumArtistRepository.existsById(newId))
            throw new AlbumArtistAlreadyExistsException("The target artist is already linked to this album.");
        albumArtistRepository.deleteById(new AlbumArtistId(albumId, artistId));
        albumArtistRepository.save(AlbumArtist.builder()
                .albumId(albumId)
                .artistId(newArtistId)
                .role(current.getRole())
                .order(current.getOrder())
                .build());
    }

    @Transactional
    public void updateRole(UUID albumId, UUID artistId, ArtistRole role) {
        var albumArtist = findById(albumId, artistId);
        albumArtist.setRole(role);
        albumArtistRepository.save(albumArtist);
    }

    @Transactional
    public void updateOrder(UUID albumId, UUID artistId, Integer order) {
        var albumArtist = findById(albumId, artistId);
        albumArtist.setOrder(order);
        albumArtistRepository.save(albumArtist);
    }

    @Transactional
    public void delete(UUID albumId, UUID artistId) {
        findById(albumId, artistId);
        albumArtistRepository.deleteById(new AlbumArtistId(albumId, artistId));
    }

    private AlbumArtist findById(UUID albumId, UUID artistId) {
        return albumArtistRepository.findById(new AlbumArtistId(albumId, artistId))
                .orElseThrow(() -> new AlbumArtistNotFoundException("Failed to find album artist."));
    }
}
