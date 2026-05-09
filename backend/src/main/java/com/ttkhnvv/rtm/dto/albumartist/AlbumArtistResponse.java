package com.ttkhnvv.rtm.dto.albumartist;

import com.ttkhnvv.rtm.entity.albumartist.ArtistRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumArtistResponse {
    private UUID albumId;
    private UUID artistId;
    private ArtistRole role;
    private Integer order;
}
