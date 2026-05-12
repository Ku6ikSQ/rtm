package com.ttkhnvv.rtm.repository.albumgenre;

import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenre;
import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AlbumGenreRepository extends JpaRepository<AlbumGenre, AlbumGenreId> {
    List<AlbumGenre> findAllByAlbumId(UUID albumId);

    long countByGenreId(UUID genreId);

    @Query("SELECT ag.genreId, COUNT(ag) FROM AlbumGenre ag WHERE ag.genreId IN :genreIds GROUP BY ag.genreId")
    List<Object[]> countGroupedByGenreIdIn(@Param("genreIds") List<UUID> genreIds);
}
