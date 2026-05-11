package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.pagination.PageResponse;
import com.ttkhnvv.rtm.dto.track.CreateTrackRequest;
import com.ttkhnvv.rtm.dto.track.TrackFilter;
import com.ttkhnvv.rtm.dto.track.TrackResponse;
import com.ttkhnvv.rtm.entity.track.Track;
import com.ttkhnvv.rtm.exception.track.TrackNotFoundException;
import com.ttkhnvv.rtm.exception.track.TrackPositionAlreadyTakenException;
import com.ttkhnvv.rtm.mapper.TrackMapper;
import com.ttkhnvv.rtm.repository.track.TrackRepository;
import com.ttkhnvv.rtm.repository.track.TrackSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Manages track catalog operations and enforces unique track number per album.
 * Supports filtering by title substring and album.
 */
@Service
@RequiredArgsConstructor
public class TrackService {
    private final TrackRepository trackRepository;
    private final TrackMapper trackMapper;

    /**
     * Returns a paginated list of tracks matching the given filter criteria.
     *
     * @param filter   optional filters (title substring, album id)
     * @param pageable pagination and sorting parameters
     * @return page of track responses
     */
    @Transactional(readOnly = true)
    public PageResponse<TrackResponse> getAll(TrackFilter filter, Pageable pageable) {
        var spec = TrackSpecs.titleContains(filter.getTitle())
                .and(TrackSpecs.albumIdEquals(filter.getAlbumId()));
        var page = trackRepository.findAll(spec, pageable);
        var content = page.getContent().stream()
                .map(trackMapper::toResponse)
                .toList();
        return PageResponse.of(page, content);
    }

    /**
     * Returns a single track by its identifier.
     *
     * @param id track identifier
     * @return track response
     * @throws TrackNotFoundException if no track was found with the given id
     */
    @Transactional(readOnly = true)
    public TrackResponse getById(UUID id) {
        return trackMapper.toResponse(findTrackById(id));
    }

    /**
     * Creates a new track, ensuring the track number is unique within the album.
     *
     * @param request track data (title, albumId, track number, duration)
     * @return the created track response
     * @throws TrackPositionAlreadyTakenException if the track number is already occupied in the album
     */
    @Transactional
    public TrackResponse create(CreateTrackRequest request) {
        checkPositionAvailable(request.getAlbumId(), request.getTrackNumber());
        var track = Track.builder()
                .title(request.getTitle())
                .albumId(request.getAlbumId())
                .trackNumber(request.getTrackNumber())
                .durationSeconds(request.getDurationSeconds())
                .build();
        return trackMapper.toResponse(trackRepository.save(track));
    }

    /**
     * Updates the title of a track.
     *
     * @param id    track identifier
     * @param title new title
     * @throws TrackNotFoundException if no track was found with the given id
     */
    @Transactional
    public void updateTitle(UUID id, String title) {
        var track = findTrackById(id);
        track.setTitle(title);
        trackRepository.save(track);
    }

    /**
     * Moves a track to a different album, ensuring its current track number is not already occupied there.
     *
     * @param id      track identifier
     * @param albumId target album identifier
     * @throws TrackNotFoundException             if no track was found with the given id
     * @throws TrackPositionAlreadyTakenException if the track number is already occupied in the target album
     */
    @Transactional
    public void updateAlbumId(UUID id, UUID albumId) {
        var track = findTrackById(id);
        if (trackRepository.existsByAlbumIdAndTrackNumberAndIdNot(albumId, track.getTrackNumber(), id))
            throw new TrackPositionAlreadyTakenException(
                    "Track number " + track.getTrackNumber() + " is already taken in the target album.");
        track.setAlbumId(albumId);
        trackRepository.save(track);
    }

    /**
     * Updates the track number within its album, ensuring the new position is not already occupied.
     *
     * @param id          track identifier
     * @param trackNumber new track number
     * @throws TrackNotFoundException             if no track was found with the given id
     * @throws TrackPositionAlreadyTakenException if the track number is already occupied in the album
     */
    @Transactional
    public void updateTrackNumber(UUID id, Integer trackNumber) {
        var track = findTrackById(id);
        if (trackRepository.existsByAlbumIdAndTrackNumberAndIdNot(track.getAlbumId(), trackNumber, id))
            throw new TrackPositionAlreadyTakenException(
                    "Track number " + trackNumber + " is already taken in this album.");
        track.setTrackNumber(trackNumber);
        trackRepository.save(track);
    }

    /**
     * Updates the duration of a track in seconds.
     *
     * @param id              track identifier
     * @param durationSeconds new duration in seconds
     * @throws TrackNotFoundException if no track was found with the given id
     */
    @Transactional
    public void updateDurationSeconds(UUID id, Integer durationSeconds) {
        var track = findTrackById(id);
        track.setDurationSeconds(durationSeconds);
        trackRepository.save(track);
    }

    /**
     * Deletes a track by its identifier.
     *
     * @param id track identifier
     * @throws TrackNotFoundException if no track was found with the given id
     */
    @Transactional
    public void delete(UUID id) {
        findTrackById(id);
        trackRepository.deleteById(id);
    }

    private void checkPositionAvailable(UUID albumId, Integer trackNumber) {
        if (trackRepository.existsByAlbumIdAndTrackNumber(albumId, trackNumber))
            throw new TrackPositionAlreadyTakenException(
                    "Track number " + trackNumber + " is already taken in this album.");
    }

    private Track findTrackById(UUID id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> new TrackNotFoundException("Failed to find track."));
    }
}
