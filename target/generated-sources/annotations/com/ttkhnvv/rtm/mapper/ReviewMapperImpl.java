package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.review.ReviewResponse;
import com.ttkhnvv.rtm.entity.review.Review;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-11T11:25:47+0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public ReviewResponse toResponse(Review review) {
        if ( review == null ) {
            return null;
        }

        ReviewResponse.ReviewResponseBuilder reviewResponse = ReviewResponse.builder();

        reviewResponse.id( review.getId() );
        reviewResponse.albumId( review.getAlbumId() );
        reviewResponse.authorId( review.getAuthorId() );
        reviewResponse.score( review.getScore() );
        reviewResponse.reviewText( review.getReviewText() );
        reviewResponse.createdAt( review.getCreatedAt() );
        reviewResponse.updatedAt( review.getUpdatedAt() );

        return reviewResponse.build();
    }
}
