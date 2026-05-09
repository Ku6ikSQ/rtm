package com.ttkhnvv.rtm.repository.track;

import com.ttkhnvv.rtm.entity.track.Track;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

@UtilityClass
public class TrackSpecs {
    public Specification<Track> titleContains(String title) {
        return (root, query, cb) ->
                title == null ? null : cb.like(cb.lower(root.get("title")), String.format("%%%s%%", title.toLowerCase()));
    }

    public Specification<Track> albumIdEquals(UUID albumId) {
        return (root, query, cb) ->
                albumId == null ? null : cb.equal(root.get("albumId"), albumId);
    }
}
