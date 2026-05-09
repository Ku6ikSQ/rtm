package com.ttkhnvv.rtm.repository.artist;

import com.ttkhnvv.rtm.entity.artist.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArtistRepository extends JpaRepository<Artist, UUID> {
}
