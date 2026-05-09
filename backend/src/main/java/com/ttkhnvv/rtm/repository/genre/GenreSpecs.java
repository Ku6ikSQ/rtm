package com.ttkhnvv.rtm.repository.genre;

import com.ttkhnvv.rtm.entity.genre.Genre;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

@UtilityClass
public class GenreSpecs {
    public Specification<Genre> nameContains(String name) {
        return (root, query, cb) ->
                name == null ? null : cb.like(cb.lower(root.get("name")), String.format("%%%s%%", name.toLowerCase()));
    }

    public Specification<Genre> parentIdEquals(UUID parentId) {
        return (root, query, cb) ->
                parentId == null ? null : cb.equal(root.get("parentId"), parentId);
    }
}
