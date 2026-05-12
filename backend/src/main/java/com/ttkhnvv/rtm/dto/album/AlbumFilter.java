package com.ttkhnvv.rtm.dto.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumFilter {
    private String title;
    private Integer yearFrom;
    private Integer yearTo;
    private BigDecimal ratingMin;
    private BigDecimal ratingMax;
    private UUID genreId;
    private UUID artistId;
}
