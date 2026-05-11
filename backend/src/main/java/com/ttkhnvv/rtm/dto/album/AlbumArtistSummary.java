package com.ttkhnvv.rtm.dto.album;

import com.ttkhnvv.rtm.entity.albumartist.ArtistRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumArtistSummary {
    private UUID albumId;
    private UUID artistId;
    private String stageName;
    private ArtistRole role;
    private Integer order;
}