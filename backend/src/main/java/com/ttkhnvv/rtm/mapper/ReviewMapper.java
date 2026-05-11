package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.review.ReviewResponse;
import com.ttkhnvv.rtm.entity.review.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "authorUsername", ignore = true)
    @Mapping(target = "authorImageUrl", ignore = true)
    ReviewResponse toResponse(Review review);
}
