package com.ttkhnvv.rtm.repository.album;

import com.ttkhnvv.rtm.entity.album.Album;
import com.ttkhnvv.rtm.entity.albumartist.AlbumArtist;
import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenre;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

@UtilityClass
public class AlbumSpecs {
    public Specification<Album> titleContains(String title) {
        return (root, query, cb) ->
                title == null ? null : cb.like(cb.lower(root.get("title")), String.format("%%%s%%", title.toLowerCase()));
    }

    public Specification<Album> releaseYearEquals(Integer year) {
        return (root, query, cb) ->
                year == null ? null : cb.equal(root.get("releaseYear"), year);
    }

    public Specification<Album> byGenreId(UUID genreId) {
        return (root, query, cb) -> {
            if (genreId == null) return null;
            var subquery = query.subquery(UUID.class);
            var agRoot = subquery.from(AlbumGenre.class);
            subquery.select(agRoot.get("albumId"))
                    .where(cb.equal(agRoot.get("genreId"), genreId));
            return root.get("id").in(subquery);
        };
    }

    public Specification<Album> byArtistId(UUID artistId) {
        return (root, query, cb) -> {
            if (artistId == null) return null;
            var subquery = query.subquery(UUID.class);
            var aaRoot = subquery.from(AlbumArtist.class);
            subquery.select(aaRoot.get("albumId"))
                    .where(cb.equal(aaRoot.get("artistId"), artistId));
            return root.get("id").in(subquery);
        };
    }
}

