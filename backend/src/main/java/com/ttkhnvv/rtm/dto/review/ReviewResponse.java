package com.ttkhnvv.rtm.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private UUID id;
    private UUID albumId;
    private UUID authorId;
    private Integer score;
    private String reviewText;
    private Instant createdAt;
    private Instant updatedAt;
    private String authorUsername;
    private String authorImageUrl;
}
