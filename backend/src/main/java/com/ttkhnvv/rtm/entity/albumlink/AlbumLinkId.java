package com.ttkhnvv.rtm.entity.albumlink;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumLinkId implements Serializable {
    private UUID albumId;
    private UUID platformId;
}