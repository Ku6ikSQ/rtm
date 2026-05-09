package com.ttkhnvv.rtm.entity.albumartist;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@Table(name = "album_artists")
@IdClass(AlbumArtistId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumArtist {
    @Id
    @Column(name = "album_id")
    private UUID albumId;

    @Id
    @Column(name = "artist_id")
    private UUID artistId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "role")
    private ArtistRole role;

    @Column(name = "`order`", nullable = false)
    private Integer order;
}
