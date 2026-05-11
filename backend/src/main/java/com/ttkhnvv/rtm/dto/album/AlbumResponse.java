package com.ttkhnvv.rtm.dto.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumResponse {
    private UUID id;
    private String title;
    private String description;
    private Integer releaseYear;
    private String coverUrl;
    private BigDecimal avgRating;
    private Instant createdAt;
    private UUID createdBy;
    private long reviewCount;
    private List<AlbumArtistSummary> artists;
}
