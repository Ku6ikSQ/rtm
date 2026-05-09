package com.ttkhnvv.rtm.dto.albumgenre;

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
public class CreateAlbumGenreRequest {
    @NotNull(message = "Album ID is required")
    private UUID albumId;
    @NotNull(message = "Genre ID is required")
    private UUID genreId;
}