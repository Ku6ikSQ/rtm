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

/**
 * Manages artist profiles including bio, stage/real name, country and photo storage.
 * Artist photos are stored in object storage; presigned URLs are generated on read.
 */
@Service
@RequiredArgsConstructor
public class ArtistService {
    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;
    private final StorageService storageService;

    /**
     * Returns a single artist by their identifier.
     * The image URL in the response is presigned and valid for 60 minutes.
     *
     * @param id artist identifier
     * @return artist response with a presigned image URL if a photo is present
     * @throws ArtistNotFoundException if no artist was found with the given id
     */
    @Transactional(readOnly = true)
    public ArtistResponse getById(UUID id) {
        var artist = findArtistById(id);
        var response = artistMapper.toResponse(artist);
        if (artist.getImageKey() != null)
            response.setImageUrl(storageService.getPresignedUrl(artist.getImageKey()));
        return response;
    }

    /**
     * Creates a new artist profile.
     *
     * @param request artist data (stage name, real name, bio, country)
     * @return the created artist response
     */
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

    /**
     * Updates the stage name of an artist.
     *
     * @param id        artist identifier
     * @param stageName new stage name
     * @throws ArtistNotFoundException if no artist was found with the given id
     */
    @Transactional
    public void updateStageName(UUID id, String stageName) {
        var artist = findArtistById(id);
        artist.setStageName(stageName);
        artistRepository.save(artist);
    }

    /**
     * Updates the real name of an artist.
     *
     * @param id       artist identifier
     * @param realName new real name
     * @throws ArtistNotFoundException if no artist was found with the given id
     */
    @Transactional
    public void updateRealName(UUID id, String realName) {
        var artist = findArtistById(id);
        artist.setRealName(realName);
        artistRepository.save(artist);
    }

    /**
     * Updates the biography of an artist.
     *
     * @param id  artist identifier
     * @param bio new biography text
     * @throws ArtistNotFoundException if no artist was found with the given id
     */
    @Transactional
    public void updateBio(UUID id, String bio) {
        var artist = findArtistById(id);
        artist.setBio(bio);
        artistRepository.save(artist);
    }

    /**
     * Updates the country of an artist.
     *
     * @param id      artist identifier
     * @param country new country value
     * @throws ArtistNotFoundException if no artist was found with the given id
     */
    @Transactional
    public void updateCountry(UUID id, String country) {
        var artist = findArtistById(id);
        artist.setCountry(country);
        artistRepository.save(artist);
    }

    /**
     * Replaces the photo of an artist. Deletes the previous photo from storage if one exists.
     *
     * @param id   artist identifier
     * @param file new photo (jpeg, png or webp)
     * @throws ArtistNotFoundException if no artist was found with the given id
     */
    @Transactional
    public void updatePhoto(UUID id, MultipartFile file) {
        var artist = findArtistById(id);
        if (artist.getImageKey() != null)
            storageService.delete(artist.getImageKey());
        artist.setImageKey(storageService.upload(file));
        artistRepository.save(artist);
    }

    /**
     * Deletes an artist and their photo from storage if one exists.
     *
     * @param id artist identifier
     * @throws ArtistNotFoundException if no artist was found with the given id
     */
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
