package com.ttkhnvv.rtm.repository.album;

import com.ttkhnvv.rtm.entity.album.Album;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

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
}

