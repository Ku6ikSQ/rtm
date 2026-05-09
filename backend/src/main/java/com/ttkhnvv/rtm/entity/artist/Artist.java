package com.ttkhnvv.rtm.entity.artist;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "artists")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "stage_name", nullable = false)
    private String stageName;

    @Column(name = "real_name")
    private String realName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "country")
    private String country;

    @Column(name = "image_key")
    private String imageKey;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}