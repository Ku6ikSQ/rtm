package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.review.CreateReviewRequest;
import com.ttkhnvv.rtm.dto.review.ReviewFilter;
import com.ttkhnvv.rtm.dto.review.ReviewResponse;
import com.ttkhnvv.rtm.entity.review.Review;
import com.ttkhnvv.rtm.exception.review.ReviewAlreadyExistsException;
import com.ttkhnvv.rtm.exception.review.ReviewNotFoundException;
import com.ttkhnvv.rtm.mapper.ReviewMapper;
import com.ttkhnvv.rtm.repository.review.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private AlbumService albumService;
    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private UUID reviewId;
    private UUID albumId;
    private UUID authorId;
    private Review review;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void init() {
        reviewId = UUID.randomUUID();
        albumId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        review = Review.builder()
                .id(reviewId)
                .albumId(albumId)
                .authorId(authorId)
                .score(8)
                .reviewText("Great album")
                .build();
        reviewResponse = ReviewResponse.builder()
                .id(reviewId)
                .albumId(albumId)
                .authorId(authorId)
                .score(8)
                .reviewText("Great album")
                .build();
    }

    @Nested
    class GetAll {
        @Test
        void shouldReturnPageOfReviews_whenFilterMatches() {
            // given
            var filter = new ReviewFilter();
            filter.setAlbumId(albumId);
            var pageable = Pageable.unpaged();
            when(reviewRepository.findAll((Specification<Review>) any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(review)));
            when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

            // when
            var result = reviewService.getAll(filter, pageable);

            // then
            assertThat(result.getContent()).containsExactly(reviewResponse);
        }

        @Test
        void shouldReturnEmptyPage_whenNoReviewsMatch() {
            // given
            var filter = new ReviewFilter();
            var pageable = Pageable.unpaged();
            when(reviewRepository.findAll((Specification<Review>) any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            // when
            var result = reviewService.getAll(filter, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    class GetById {
        @Test
        void shouldReturnReviewResponse_whenReviewExists() {
            // given
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

            // when
            var result = reviewService.getById(reviewId);

            // then
            assertThat(result).isEqualTo(reviewResponse);
        }

        @Test
        void shouldThrowException_whenReviewNotFound() {
            // given
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> reviewService.getById(reviewId))
                    .isInstanceOf(ReviewNotFoundException.class);
        }
    }

    @Nested
    class Create {
        @Test
        void shouldCreateReviewAndRecalculateRating_whenAuthorHasNoReviewForAlbum() {
            // given
            var request = CreateReviewRequest.builder()
                    .albumId(albumId)
                    .score(8)
                    .reviewText("Great album")
                    .build();
            when(reviewRepository.existsByAlbumIdAndAuthorId(albumId, authorId)).thenReturn(false);
            when(reviewRepository.save(any(Review.class))).thenReturn(review);
            when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

            // when
            var result = reviewService.create(request, authorId);

            // then
            assertThat(result).isEqualTo(reviewResponse);
            verify(albumService).recalculateRating(albumId);
        }

        @Test
        void shouldThrowException_whenAuthorAlreadyReviewedAlbum() {
            // given
            var request = CreateReviewRequest.builder()
                    .albumId(albumId)
                    .score(8)
                    .reviewText("Great album")
                    .build();
            when(reviewRepository.existsByAlbumIdAndAuthorId(albumId, authorId)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> reviewService.create(request, authorId))
                    .isInstanceOf(ReviewAlreadyExistsException.class);
            verify(reviewRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateAlbumId {
        @Test
        void shouldUpdateAlbumAndRecalculateBothRatings_whenTargetAlbumIsAvailable() {
            // given
            var newAlbumId = UUID.randomUUID();
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewRepository.existsByAlbumIdAndAuthorIdAndIdNot(newAlbumId, authorId, reviewId)).thenReturn(false);

            // when
            reviewService.updateAlbumId(reviewId, newAlbumId);

            // then
            assertThat(review.getAlbumId()).isEqualTo(newAlbumId);
            verify(reviewRepository).save(review);
            verify(albumService).recalculateRating(albumId);
            verify(albumService).recalculateRating(newAlbumId);
        }

        @Test
        void shouldThrowException_whenReviewNotFound() {
            // given
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> reviewService.updateAlbumId(reviewId, UUID.randomUUID()))
                    .isInstanceOf(ReviewNotFoundException.class);
        }

        @Test
        void shouldThrowException_whenAuthorAlreadyHasReviewForTargetAlbum() {
            // given
            var newAlbumId = UUID.randomUUID();
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewRepository.existsByAlbumIdAndAuthorIdAndIdNot(newAlbumId, authorId, reviewId)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> reviewService.updateAlbumId(reviewId, newAlbumId))
                    .isInstanceOf(ReviewAlreadyExistsException.class);
        }
    }

    @Nested
    class UpdateAuthorId {
        @Test
        void shouldUpdateAuthor_whenTargetAuthorHasNoReviewForAlbum() {
            // given
            var newAuthorId = UUID.randomUUID();
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewRepository.existsByAlbumIdAndAuthorIdAndIdNot(albumId, newAuthorId, reviewId)).thenReturn(false);

            // when
            reviewService.updateAuthorId(reviewId, newAuthorId);

            // then
            assertThat(review.getAuthorId()).isEqualTo(newAuthorId);
            verify(reviewRepository).save(review);
        }

        @Test
        void shouldThrowException_whenReviewNotFound() {
            // given
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> reviewService.updateAuthorId(reviewId, UUID.randomUUID()))
                    .isInstanceOf(ReviewNotFoundException.class);
        }

        @Test
        void shouldThrowException_whenTargetAuthorAlreadyHasReviewForAlbum() {
            // given
            var newAuthorId = UUID.randomUUID();
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewRepository.existsByAlbumIdAndAuthorIdAndIdNot(albumId, newAuthorId, reviewId)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> reviewService.updateAuthorId(reviewId, newAuthorId))
                    .isInstanceOf(ReviewAlreadyExistsException.class);
        }
    }

    @Nested
    class UpdateScore {
        @Test
        void shouldUpdateScoreAndRecalculateRating_whenReviewExists() {
            // given
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            // when
            reviewService.updateScore(reviewId, 10);

            // then
            assertThat(review.getScore()).isEqualTo(10);
            verify(reviewRepository).save(review);
            verify(albumService).recalculateRating(albumId);
        }

        @Test
        void shouldThrowException_whenReviewNotFound() {
            // given
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> reviewService.updateScore(reviewId, 10))
                    .isInstanceOf(ReviewNotFoundException.class);
        }
    }

    @Nested
    class UpdateReviewText {
        @Test
        void shouldUpdateReviewText_whenReviewExists() {
            // given
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            // when
            reviewService.updateReviewText(reviewId, "Updated text");

            // then
            assertThat(review.getReviewText()).isEqualTo("Updated text");
            verify(reviewRepository).save(review);
            verify(albumService, never()).recalculateRating(any());
        }

        @Test
        void shouldThrowException_whenReviewNotFound() {
            // given
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> reviewService.updateReviewText(reviewId, "Updated text"))
                    .isInstanceOf(ReviewNotFoundException.class);
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeleteReviewAndRecalculateRating_whenReviewExists() {
            // given
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            // when
            reviewService.delete(reviewId);

            // then
            verify(reviewRepository).deleteById(reviewId);
            verify(albumService).recalculateRating(albumId);
        }

        @Test
        void shouldThrowException_whenReviewNotFound() {
            // given
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> reviewService.delete(reviewId))
                    .isInstanceOf(ReviewNotFoundException.class);
            verify(reviewRepository, never()).deleteById(any());
        }
    }
}
