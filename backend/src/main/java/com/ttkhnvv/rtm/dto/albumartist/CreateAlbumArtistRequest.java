package com.ttkhnvv.rtm.dto.albumartist;

import com.ttkhnvv.rtm.entity.albumartist.ArtistRole;
import com.ttkhnvv.rtm.validation.albumartist.ValidOrder;
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
public class CreateAlbumArtistRequest {
    @NotNull(message = "Album ID is required")
    private UUID albumId;
    @NotNull(message = "Artist ID is required")
    private UUID artistId;
    private ArtistRole role;
    @ValidOrder
    private Integer order;
}
