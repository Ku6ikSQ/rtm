package com.ttkhnvv.rtm.entity.albumgenre;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "album_genres")
@IdClass(AlbumGenreId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumGenre {
    @Id
    @Column(name = "album_id")
    private UUID albumId;

    @Id
    @Column(name = "genre_id")
    private UUID genreId;
}
