package com.ttkhnvv.rtm.repository.platform;

import com.ttkhnvv.rtm.entity.platform.Platform;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class PlatformSpecs {
    public Specification<Platform> nameContains(String name) {
        return (root, query, cb) ->
                name == null ? null : cb.like(cb.lower(root.get("name")), String.format("%%%s%%", name.toLowerCase()));
    }
}
