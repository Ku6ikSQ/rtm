package com.ttkhnvv.rtm.dto.albumartist;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateArtistIdRequest {
    @NotNull(message = "Artist ID is required")
    private UUID artistId;
}
