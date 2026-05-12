package com.ttkhnvv.rtm.repository.albumlink;

import com.ttkhnvv.rtm.entity.albumlink.AlbumLink;
import com.ttkhnvv.rtm.entity.albumlink.AlbumLinkId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlbumLinkRepository extends JpaRepository<AlbumLink, AlbumLinkId> {
    List<AlbumLink> findAllByAlbumId(UUID albumId);
}