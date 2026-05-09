package com.ttkhnvv.rtm.repository.album;

import com.ttkhnvv.rtm.entity.album.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AlbumRepository extends JpaRepository<Album, UUID>,
        JpaSpecificationExecutor<Album> {
}
