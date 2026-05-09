package com.ttkhnvv.rtm.repository.albumgenre;

import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenre;
import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenreId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlbumGenreRepository extends JpaRepository<AlbumGenre, AlbumGenreId> {
    List<AlbumGenre> findAllByAlbumId(UUID albumId);
}
