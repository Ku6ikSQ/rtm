package com.ttkhnvv.rtm.repository.artist;

import com.ttkhnvv.rtm.entity.artist.Artist;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class ArtistSpecs {
    public Specification<Artist> stageNameContains(String stageName) {
        return (root, query, cb) ->
                stageName == null ? null : cb.like(cb.lower(root.get("stageName")), String.format("%%%s%%", stageName.toLowerCase()));
    }
}