package com.ttkhnvv.rtm.repository.track;

import com.ttkhnvv.rtm.entity.track.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID>,
        JpaSpecificationExecutor<Track> {
    boolean existsByAlbumIdAndTrackNumber(UUID albumId, Integer trackNumber);
    boolean existsByAlbumIdAndTrackNumberAndIdNot(UUID albumId, Integer trackNumber, UUID id);
}
