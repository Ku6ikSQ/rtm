package com.ttkhnvv.rtm.repository.albumartist;

import com.ttkhnvv.rtm.entity.albumartist.AlbumArtist;
import com.ttkhnvv.rtm.entity.albumartist.AlbumArtistId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlbumArtistRepository extends JpaRepository<AlbumArtist, AlbumArtistId> {
    List<AlbumArtist> findAllByAlbumId(UUID albumId);
    List<AlbumArtist> findAllByAlbumIdIn(List<UUID> albumIds);
}
