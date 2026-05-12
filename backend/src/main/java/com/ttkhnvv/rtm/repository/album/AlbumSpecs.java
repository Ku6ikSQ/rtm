package com.ttkhnvv.rtm.repository.album;

import com.ttkhnvv.rtm.entity.album.Album;
import com.ttkhnvv.rtm.entity.albumartist.AlbumArtist;
import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenre;
import com.ttkhnvv.rtm.entity.artist.Artist;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

@UtilityClass
public class AlbumSpecs {
    public Specification<Album> titleOrArtistContains(String query) {
        return (root, q, cb) -> {
            if (query == null) return null;
            String pattern = "%" + query.toLowerCase() + "%";

            // SELECT id FROM artists WHERE LOWER(stage_name) LIKE pattern
            var artistIdSubquery = q.subquery(UUID.class);
            var artistRoot = artistIdSubquery.from(Artist.class);
            artistIdSubquery.select(artistRoot.<UUID>get("id"))
                    .where(cb.like(cb.lower(artistRoot.get("stageName")), pattern));

            // SELECT album_id FROM album_artists WHERE artist_id IN (above)
            var albumIdSubquery = q.subquery(UUID.class);
            var aaRoot = albumIdSubquery.from(AlbumArtist.class);
            albumIdSubquery.select(aaRoot.<UUID>get("albumId"))
                    .where(aaRoot.get("artistId").in(artistIdSubquery));

            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    root.get("id").in(albumIdSubquery)
            );
        };
    }

    public Specification<Album> releaseYearFrom(Integer yearFrom) {
        return (root, query, cb) ->
                yearFrom == null ? null : cb.greaterThanOrEqualTo(root.<Integer>get("releaseYear"), yearFrom);
    }

    public Specification<Album> releaseYearTo(Integer yearTo) {
        return (root, query, cb) ->
                yearTo == null ? null : cb.lessThanOrEqualTo(root.<Integer>get("releaseYear"), yearTo);
    }

    public Specification<Album> ratingMin(BigDecimal min) {
        return (root, query, cb) ->
                min == null ? null : cb.greaterThanOrEqualTo(root.<BigDecimal>get("avgRating"), min);
    }

    public Specification<Album> ratingMax(BigDecimal max) {
        return (root, query, cb) ->
                max == null ? null : cb.or(
                        root.<BigDecimal>get("avgRating").isNull(),
                        cb.lessThanOrEqualTo(root.<BigDecimal>get("avgRating"), max)
                );
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

