package com.ttkhnvv.rtm.repository.album;

import com.ttkhnvv.rtm.entity.album.Album;
import com.ttkhnvv.rtm.entity.albumartist.AlbumArtist;
import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenre;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

@UtilityClass
public class AlbumSpecs {
    public Specification<Album> titleContains(String title) {
        return (root, query, cb) ->
                title == null ? null : cb.like(cb.lower(root.get("title")), String.format("%%%s%%", title.toLowerCase()));
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

