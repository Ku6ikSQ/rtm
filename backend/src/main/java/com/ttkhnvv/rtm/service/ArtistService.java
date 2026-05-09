package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.artist.ArtistResponse;
import com.ttkhnvv.rtm.dto.artist.CreateArtistRequest;
import com.ttkhnvv.rtm.entity.artist.Artist;
import com.ttkhnvv.rtm.exception.artist.ArtistNotFoundException;
import com.ttkhnvv.rtm.mapper.ArtistMapper;
import com.ttkhnvv.rtm.repository.artist.ArtistRepository;
import com.ttkhnvv.rtm.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistService {
    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public ArtistResponse getById(UUID id) {
        var artist = findArtistById(id);
        var response = artistMapper.toResponse(artist);
        if (artist.getImageKey() != null)
            response.setImageUrl(storageService.getPresignedUrl(artist.getImageKey()));
        return response;
    }

    @Transactional
    public ArtistResponse create(CreateArtistRequest request) {
        var artist = Artist.builder()
                .stageName(request.getStageName())
                .realName(request.getRealName())
                .bio(request.getBio())
                .country(request.getCountry())
                .build();
        return artistMapper.toResponse(artistRepository.save(artist));
    }

    @Transactional
    public void updateStageName(UUID id, String stageName) {
        var artist = findArtistById(id);
        artist.setStageName(stageName);
        artistRepository.save(artist);
    }

    @Transactional
    public void updateRealName(UUID id, String realName) {
        var artist = findArtistById(id);
        artist.setRealName(realName);
        artistRepository.save(artist);
    }

    @Transactional
    public void updateBio(UUID id, String bio) {
        var artist = findArtistById(id);
        artist.setBio(bio);
        artistRepository.save(artist);
    }

    @Transactional
    public void updateCountry(UUID id, String country) {
        var artist = findArtistById(id);
        artist.setCountry(country);
        artistRepository.save(artist);
    }

    @Transactional
    public void updatePhoto(UUID id, MultipartFile file) {
        var artist = findArtistById(id);
        if (artist.getImageKey() != null)
            storageService.delete(artist.getImageKey());
        artist.setImageKey(storageService.upload(file));
        artistRepository.save(artist);
    }

    @Transactional
    public void delete(UUID id) {
        var artist = findArtistById(id);
        if (artist.getImageKey() != null)
            storageService.delete(artist.getImageKey());
        artistRepository.deleteById(id);
    }

    private Artist findArtistById(UUID id) {
        return artistRepository.findById(id)
                .orElseThrow(() ->
                        new ArtistNotFoundException("Failed to find artist."));
    }
}
