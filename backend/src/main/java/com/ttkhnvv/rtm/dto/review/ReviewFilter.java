package com.ttkhnvv.rtm.dto.review;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewFilter {
    private UUID albumId;
    private UUID authorId;
}
