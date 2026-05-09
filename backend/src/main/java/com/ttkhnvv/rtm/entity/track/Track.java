package com.ttkhnvv.rtm.entity.track;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tracks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"album_id", "track_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "album_id", nullable = false)
    private UUID albumId;

    @Column(name = "track_number", nullable = false)
    private Integer trackNumber;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;
}
