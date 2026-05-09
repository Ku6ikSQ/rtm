package com.ttkhnvv.rtm.dto.artist;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArtistResponse {
    private UUID id;
    private String stageName;
    private String realName;
    private String bio;
    private String country;
    private String imageUrl;
    private Instant createdAt;
}
