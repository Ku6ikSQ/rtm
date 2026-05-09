package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.track.CreateTrackRequest;
import com.ttkhnvv.rtm.dto.track.TrackResponse;
import com.ttkhnvv.rtm.entity.track.Track;
import com.ttkhnvv.rtm.exception.track.TrackNotFoundException;
import com.ttkhnvv.rtm.exception.track.TrackPositionAlreadyTakenException;
import com.ttkhnvv.rtm.mapper.TrackMapper;
import com.ttkhnvv.rtm.repository.track.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackService {
    private final TrackRepository trackRepository;
    private final TrackMapper trackMapper;

    @Transactional(readOnly = true)
    public List<TrackResponse> getAll() {
        return trackRepository.findAll().stream()
                .map(trackMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrackResponse getById(UUID id) {
        return trackMapper.toResponse(findTrackById(id));
    }

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

    @Transactional
    public void updateTitle(UUID id, String title) {
        var track = findTrackById(id);
        track.setTitle(title);
        trackRepository.save(track);
    }

    @Transactional
    public void updateAlbumId(UUID id, UUID albumId) {
        var track = findTrackById(id);
        if (trackRepository.existsByAlbumIdAndTrackNumberAndIdNot(albumId, track.getTrackNumber(), id))
            throw new TrackPositionAlreadyTakenException(
                    "Track number " + track.getTrackNumber() + " is already taken in the target album.");
        track.setAlbumId(albumId);
        trackRepository.save(track);
    }

    @Transactional
    public void updateTrackNumber(UUID id, Integer trackNumber) {
        var track = findTrackById(id);
        if (trackRepository.existsByAlbumIdAndTrackNumberAndIdNot(track.getAlbumId(), trackNumber, id))
            throw new TrackPositionAlreadyTakenException(
                    "Track number " + trackNumber + " is already taken in this album.");
        track.setTrackNumber(trackNumber);
        trackRepository.save(track);
    }

    @Transactional
    public void updateDurationSeconds(UUID id, Integer durationSeconds) {
        var track = findTrackById(id);
        track.setDurationSeconds(durationSeconds);
        trackRepository.save(track);
    }

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
