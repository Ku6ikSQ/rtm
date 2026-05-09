package com.ttkhnvv.rtm.entity.albumartist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumArtistId implements Serializable {
    private UUID albumId;
    private UUID artistId;
}
