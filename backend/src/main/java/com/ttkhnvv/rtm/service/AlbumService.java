package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.album.AlbumResponse;
import com.ttkhnvv.rtm.dto.album.CreateAlbumRequest;
import com.ttkhnvv.rtm.entity.album.Album;
import com.ttkhnvv.rtm.exception.album.AlbumNotFoundException;
import com.ttkhnvv.rtm.mapper.AlbumMapper;
import com.ttkhnvv.rtm.repository.album.AlbumRepository;
import com.ttkhnvv.rtm.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final AlbumMapper albumMapper;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public List<AlbumResponse> getAll() {
        return albumRepository.findAll().stream()
                .map(this::toResponseWithCoverUrl)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlbumResponse getById(UUID id) {
        return toResponseWithCoverUrl(findAlbumById(id));
    }

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

    @Transactional
    public void updateTitle(UUID id, String title) {
        var album = findAlbumById(id);
        album.setTitle(title);
        albumRepository.save(album);
    }

    @Transactional
    public void updateDescription(UUID id, String description) {
        var album = findAlbumById(id);
        album.setDescription(description);
        albumRepository.save(album);
    }

    @Transactional
    public void updateReleaseYear(UUID id, Integer releaseYear) {
        var album = findAlbumById(id);
        album.setReleaseYear(releaseYear);
        albumRepository.save(album);
    }

    @Transactional
    public void updateCover(UUID id, MultipartFile file) {
        var album = findAlbumById(id);
        if (album.getCoverKey() != null)
            storageService.delete(album.getCoverKey());
        album.setCoverKey(storageService.upload(file));
        albumRepository.save(album);
    }

    @Transactional
    public void delete(UUID id) {
        var album = findAlbumById(id);
        if (album.getCoverKey() != null)
            storageService.delete(album.getCoverKey());
        albumRepository.deleteById(id);
    }

    private AlbumResponse toResponseWithCoverUrl(Album album) {
        var response = albumMapper.toResponse(album);
        if (album.getCoverKey() != null)
            response.setCoverUrl(storageService.getPresignedUrl(album.getCoverKey()));
        return response;
    }

    private Album findAlbumById(UUID id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Failed to find album."));
    }
}
