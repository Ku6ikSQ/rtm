package com.ttkhnvv.rtm.repository.review;

import com.ttkhnvv.rtm.entity.review.Review;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

@UtilityClass
public class ReviewSpecs {
    public Specification<Review> albumIdEquals(UUID albumId) {
        return (root, query, cb) ->
                albumId == null ? null : cb.equal(root.get("albumId"), albumId);
    }

    public Specification<Review> authorIdEquals(UUID authorId) {
        return (root, query, cb) ->
                authorId == null ? null : cb.equal(root.get("authorId"), authorId);
    }
}
