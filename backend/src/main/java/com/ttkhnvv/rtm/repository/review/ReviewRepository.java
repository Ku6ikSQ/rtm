package com.ttkhnvv.rtm.repository.review;

import com.ttkhnvv.rtm.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID>,
        JpaSpecificationExecutor<Review> {
    boolean existsByAlbumIdAndAuthorId(UUID albumId, UUID authorId);
    boolean existsByAlbumIdAndAuthorIdAndIdNot(UUID albumId, UUID authorId, UUID id);

    @Query("SELECT AVG(r.score) FROM Review r WHERE r.albumId = :albumId")
    Optional<BigDecimal> calculateAvgRating(@Param("albumId") UUID albumId);

    long countByAlbumId(UUID albumId);

    @Query("SELECT r.albumId, COUNT(r) FROM Review r WHERE r.albumId IN :albumIds GROUP BY r.albumId")
    List<Object[]> countGroupedByAlbumIdIn(@Param("albumIds") List<UUID> albumIds);
}
