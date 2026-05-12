package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.pagination.PageResponse;
import com.ttkhnvv.rtm.dto.review.CreateReviewRequest;
import com.ttkhnvv.rtm.dto.review.ReviewFilter;
import com.ttkhnvv.rtm.dto.review.ReviewResponse;
import com.ttkhnvv.rtm.entity.album.Album;
import com.ttkhnvv.rtm.entity.review.Review;
import com.ttkhnvv.rtm.entity.user.User;
import com.ttkhnvv.rtm.exception.review.ReviewAlreadyExistsException;
import com.ttkhnvv.rtm.exception.review.ReviewNotFoundException;
import com.ttkhnvv.rtm.mapper.ReviewMapper;
import com.ttkhnvv.rtm.repository.album.AlbumRepository;
import com.ttkhnvv.rtm.repository.review.ReviewRepository;
import com.ttkhnvv.rtm.repository.review.ReviewSpecs;
import com.ttkhnvv.rtm.repository.user.UserRepository;
import com.ttkhnvv.rtm.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages album reviews and enforces one-review-per-user-per-album constraint.
 * Triggers album average rating recalculation whenever a score-affecting change occurs.
 * Author info is fetched in batch to avoid N+1 queries when listing reviews.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final AlbumService albumService;
    private final AlbumRepository albumRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final StorageService storageService;

    /**
     * Returns a paginated list of reviews matching the given filter criteria.
     * Author username and avatar URL are embedded in each response — no N+1.
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

        var authorIds = page.getContent().stream().map(Review::getAuthorId).distinct().toList();
        var authorMap = authorIds.isEmpty() ? Map.<UUID, User>of() :
                userRepository.findAllById(authorIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        var albumIds = page.getContent().stream().map(Review::getAlbumId).distinct().toList();
        var albumTitleMap = albumIds.isEmpty() ? Map.<UUID, String>of() :
                albumRepository.findAllById(albumIds).stream()
                        .collect(Collectors.toMap(Album::getId, Album::getTitle));

        var content = page.getContent().stream()
                .map(review -> toResponseWithDetails(review, authorMap, albumTitleMap))
                .toList();
        return PageResponse.of(page, content);
    }

    /**
     * Returns a single review by its identifier.
     * Author username and avatar URL are included in the response.
     *
     * @param id review identifier
     * @return review response with embedded author info
     * @throws ReviewNotFoundException if no review was found with the given id
     */
    @Transactional(readOnly = true)
    public ReviewResponse getById(UUID id) {
        var review = findReviewById(id);
        var author = userRepository.findById(review.getAuthorId()).orElse(null);
        var authorMap = author != null ? Map.of(author.getId(), author) : Map.<UUID, User>of();
        var album = albumRepository.findById(review.getAlbumId()).orElse(null);
        var albumTitleMap = album != null ? Map.of(album.getId(), album.getTitle()) : Map.<UUID, String>of();
        return toResponseWithDetails(review, authorMap, albumTitleMap);
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

    private ReviewResponse toResponseWithDetails(Review review, Map<UUID, User> authorMap, Map<UUID, String> albumTitleMap) {
        var response = reviewMapper.toResponse(review);
        var author = authorMap.get(review.getAuthorId());
        if (author != null) {
            response.setAuthorUsername(author.getUsername());
            if (author.getImageKey() != null)
                response.setAuthorImageUrl(storageService.getPresignedUrl(author.getImageKey()));
        }
        response.setAlbumTitle(albumTitleMap.get(review.getAlbumId()));
        return response;
    }

    private Review findReviewById(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Failed to find review."));
    }
}