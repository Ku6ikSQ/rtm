package com.ttkhnvv.rtm.dto.review;

import com.ttkhnvv.rtm.validation.review.ValidReviewText;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReviewTextRequest {
    @ValidReviewText
    private String reviewText;
}
