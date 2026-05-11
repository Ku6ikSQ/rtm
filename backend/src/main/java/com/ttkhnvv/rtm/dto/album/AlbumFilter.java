package com.ttkhnvv.rtm.dto.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumFilter {
    private String title;
    private Integer releaseYear;
    private UUID genreId;
}
