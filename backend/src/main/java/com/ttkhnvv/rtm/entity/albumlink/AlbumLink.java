package com.ttkhnvv.rtm.entity.albumlink;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "album_links")
@IdClass(AlbumLinkId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumLink {
    @Id
    @Column(name = "album_id")
    private UUID albumId;

    @Id
    @Column(name = "platform_id")
    private UUID platformId;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;
}