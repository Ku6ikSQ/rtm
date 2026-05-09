package com.ttkhnvv.rtm.dto.review;

import com.ttkhnvv.rtm.validation.review.ValidReviewText;
import com.ttkhnvv.rtm.validation.review.ValidScore;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {
    @NotNull(message = "Album ID is required")
    private UUID albumId;
    @ValidScore
    private Integer score;
    @ValidReviewText
    private String reviewText;
}
