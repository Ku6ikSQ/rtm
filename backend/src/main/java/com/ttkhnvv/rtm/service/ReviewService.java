package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.pagination.PageResponse;
import com.ttkhnvv.rtm.dto.review.CreateReviewRequest;
import com.ttkhnvv.rtm.dto.review.ReviewFilter;
import com.ttkhnvv.rtm.dto.review.ReviewResponse;
import com.ttkhnvv.rtm.entity.review.Review;
import com.ttkhnvv.rtm.exception.review.ReviewAlreadyExistsException;
import com.ttkhnvv.rtm.exception.review.ReviewNotFoundException;
import com.ttkhnvv.rtm.mapper.ReviewMapper;
import com.ttkhnvv.rtm.repository.review.ReviewRepository;
import com.ttkhnvv.rtm.repository.review.ReviewSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Manages album reviews and enforces one-review-per-user-per-album constraint.
 * Triggers album average rating recalculation whenever a score-affecting change occurs.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final AlbumService albumService;
    private final ReviewMapper reviewMapper;

    /**
     * Returns a paginated list of reviews matching the given filter criteria.
     *
     * @param filter   optional filters (album id, author id)
     * @param pageable pagination and sorting parameters
     * @return page of review responses
     */
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getAll(ReviewFilter filter, Pageable pageable) {
        var spec = ReviewSpecs.albumIdEquals(filter.getAlbumId())
                .and(ReviewSpecs.authorIdEquals(filter.getAuthorId()));
        var page = reviewRepository.findAll(spec, pageable);
        var content = page.getContent().stream()
                .map(reviewMapper::toResponse)
                .toList();
        return PageResponse.of(page, content);
    }

    /**
     * Returns a single review by its identifier.
     *
     * @param id review identifier
     * @return review response
     * @throws ReviewNotFoundException if no review was found with the given id
     */
    @Transactional(readOnly = true)
    public ReviewResponse getById(UUID id) {
        return reviewMapper.toResponse(findReviewById(id));
    }

    /**
     * Creates a new review for an album and triggers album rating recalculation.
     * Each user may have at most one review per album.
     *
     * @param request  review data (albumId, score, review text)
     * @param authorId identifier of the user submitting the review
     * @return the created review response
     * @throws ReviewAlreadyExistsException if the author has already reviewed the target album
     */
    @Transactional
    public ReviewResponse create(CreateReviewRequest request, UUID authorId) {
        if (reviewRepository.existsByAlbumIdAndAuthorId(request.getAlbumId(), authorId))
            throw new ReviewAlreadyExistsException("You have already reviewed this album.");
        var review = Review.builder()
                .albumId(request.getAlbumId())
                .authorId(authorId)
                .score(request.getScore())
                .reviewText(request.getReviewText())
                .build();
        var saved = reviewRepository.save(review);
        albumService.recalculateRating(request.getAlbumId());
        return reviewMapper.toResponse(saved);
    }

    /**
     * Moves a review to a different album and recalculates ratings for both the old and new album.
     *
     * @param id        review identifier
     * @param newAlbumId target album identifier
     * @throws ReviewNotFoundException      if no review was found with the given id
     * @throws ReviewAlreadyExistsException if the author already has a review for the target album
     */
    @Transactional
    public void updateAlbumId(UUID id, UUID newAlbumId) {
        var review = findReviewById(id);
        UUID oldAlbumId = review.getAlbumId();
        if (reviewRepository.existsByAlbumIdAndAuthorIdAndIdNot(newAlbumId, review.getAuthorId(), id))
            throw new ReviewAlreadyExistsException("This author already has a review for the target album.");
        review.setAlbumId(newAlbumId);
        reviewRepository.save(review);
        albumService.recalculateRating(oldAlbumId);
        albumService.recalculateRating(newAlbumId);
    }

    /**
     * Reassigns a review to a different author.
     *
     * @param id       review identifier
     * @param authorId target author identifier
     * @throws ReviewNotFoundException      if no review was found with the given id
     * @throws ReviewAlreadyExistsException if the target author already has a review for the same album
     */
    @Transactional
    public void updateAuthorId(UUID id, UUID authorId) {
        var review = findReviewById(id);
        if (reviewRepository.existsByAlbumIdAndAuthorIdAndIdNot(review.getAlbumId(), authorId, id))
            throw new ReviewAlreadyExistsException("The target author already has a review for this album.");
        review.setAuthorId(authorId);
        reviewRepository.save(review);
    }

    /**
     * Updates the score of a review and triggers album rating recalculation.
     *
     * @param id    review identifier
     * @param score new score value
     * @throws ReviewNotFoundException if no review was found with the given id
     */
    @Transactional
    public void updateScore(UUID id, Integer score) {
        var review = findReviewById(id);
        review.setScore(score);
        reviewRepository.save(review);
        albumService.recalculateRating(review.getAlbumId());
    }

    /**
     * Updates the text body of a review without affecting the score or album rating.
     *
     * @param id         review identifier
     * @param reviewText new review text
     * @throws ReviewNotFoundException if no review was found with the given id
     */
    @Transactional
    public void updateReviewText(UUID id, String reviewText) {
        var review = findReviewById(id);
        review.setReviewText(reviewText);
        reviewRepository.save(review);
    }

    /**
     * Deletes a review and triggers album rating recalculation.
     *
     * @param id review identifier
     * @throws ReviewNotFoundException if no review was found with the given id
     */
    @Transactional
    public void delete(UUID id) {
        var review = findReviewById(id);
        UUID albumId = review.getAlbumId();
        reviewRepository.deleteById(id);
        albumService.recalculateRating(albumId);
    }

    private Review findReviewById(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Failed to find review."));
    }
}
