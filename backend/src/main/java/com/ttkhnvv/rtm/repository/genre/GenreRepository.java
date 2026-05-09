package com.ttkhnvv.rtm.repository.genre;

import com.ttkhnvv.rtm.entity.genre.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, UUID>,
        JpaSpecificationExecutor<Genre> {
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, UUID id);
}
