package com.ttkhnvv.rtm.entity.albumgenre;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumGenreId implements Serializable {
    private UUID albumId;
    private UUID genreId;
}
