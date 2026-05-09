package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.review.CreateReviewRequest;
import com.ttkhnvv.rtm.dto.review.ReviewResponse;
import com.ttkhnvv.rtm.entity.review.Review;
import com.ttkhnvv.rtm.exception.review.ReviewAlreadyExistsException;
import com.ttkhnvv.rtm.exception.review.ReviewNotFoundException;
import com.ttkhnvv.rtm.mapper.ReviewMapper;
import com.ttkhnvv.rtm.repository.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final AlbumService albumService;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public List<ReviewResponse> getAll() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponse getById(UUID id) {
        return reviewMapper.toResponse(findReviewById(id));
    }

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

    @Transactional
    public void updateAuthorId(UUID id, UUID authorId) {
        var review = findReviewById(id);
        if (reviewRepository.existsByAlbumIdAndAuthorIdAndIdNot(review.getAlbumId(), authorId, id))
            throw new ReviewAlreadyExistsException("The target author already has a review for this album.");
        review.setAuthorId(authorId);
        reviewRepository.save(review);
    }

    @Transactional
    public void updateScore(UUID id, Integer score) {
        var review = findReviewById(id);
        review.setScore(score);
        reviewRepository.save(review);
        albumService.recalculateRating(review.getAlbumId());
    }

    @Transactional
    public void updateReviewText(UUID id, String reviewText) {
        var review = findReviewById(id);
        review.setReviewText(reviewText);
        reviewRepository.save(review);
    }

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
