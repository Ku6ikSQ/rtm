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

/**
 * Manages the many-to-many relationship between albums and artists.
 * Handles linking, role assignment, display order and composite-key re-keying.
 */
@Service
@RequiredArgsConstructor
public class AlbumArtistService {
    private final AlbumArtistRepository albumArtistRepository;
    private final AlbumArtistMapper albumArtistMapper;

    /**
     * Returns all artist links for the given album.
     *
     * @param albumId target album identifier
     * @return list of album-artist associations for the album
     */
    @Transactional(readOnly = true)
    public List<AlbumArtistResponse> getByAlbumId(UUID albumId) {
        return albumArtistRepository.findAllByAlbumId(albumId).stream()
                .map(albumArtistMapper::toResponse)
                .toList();
    }

    /**
     * Returns a single album-artist link by its composite key.
     *
     * @param albumId  album identifier
     * @param artistId artist identifier
     * @return the album-artist association
     * @throws AlbumArtistNotFoundException if no link exists for the given pair
     */
    @Transactional(readOnly = true)
    public AlbumArtistResponse getById(UUID albumId, UUID artistId) {
        return albumArtistMapper.toResponse(findById(albumId, artistId));
    }

    /**
     * Creates a new link between an album and an artist with the specified role and display order.
     *
     * @param request link data (albumId, artistId, role and order)
     * @return the created album-artist association
     * @throws AlbumArtistAlreadyExistsException if the artist is already linked to the album
     */
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

    /**
     * Moves the album-artist link to a different album by re-creating the record with the new composite key.
     *
     * @param albumId    current album identifier
     * @param artistId   artist identifier
     * @param newAlbumId target album identifier
     * @throws AlbumArtistNotFoundException      if the current link does not exist
     * @throws AlbumArtistAlreadyExistsException if the artist is already linked to the target album
     */
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

    /**
     * Replaces the artist in an album-artist link by re-creating the record with the new composite key.
     *
     * @param albumId     album identifier
     * @param artistId    current artist identifier
     * @param newArtistId target artist identifier
     * @throws AlbumArtistNotFoundException      if the current link does not exist
     * @throws AlbumArtistAlreadyExistsException if the target artist is already linked to this album
     */
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

    /**
     * Updates the role of an artist on an album.
     *
     * @param albumId  album identifier
     * @param artistId artist identifier
     * @param role     new artist role
     * @throws AlbumArtistNotFoundException if the link does not exist
     */
    @Transactional
    public void updateRole(UUID albumId, UUID artistId, ArtistRole role) {
        var albumArtist = findById(albumId, artistId);
        albumArtist.setRole(role);
        albumArtistRepository.save(albumArtist);
    }

    /**
     * Updates the display order of an artist on an album.
     *
     * @param albumId  album identifier
     * @param artistId artist identifier
     * @param order    new display order value
     * @throws AlbumArtistNotFoundException if the link does not exist
     */
    @Transactional
    public void updateOrder(UUID albumId, UUID artistId, Integer order) {
        var albumArtist = findById(albumId, artistId);
        albumArtist.setOrder(order);
        albumArtistRepository.save(albumArtist);
    }

    /**
     * Removes the link between an album and an artist.
     *
     * @param albumId  album identifier
     * @param artistId artist identifier
     * @throws AlbumArtistNotFoundException if the link does not exist
     */
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
