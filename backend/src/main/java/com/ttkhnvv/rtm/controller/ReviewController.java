package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.PageResponse;
import com.ttkhnvv.rtm.dto.review.*;
import com.ttkhnvv.rtm.security.constraint.HasRoleAny;
import com.ttkhnvv.rtm.security.constraint.HasRoleUser;
import com.ttkhnvv.rtm.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;
import static com.ttkhnvv.rtm.security.util.SecurityUtils.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @HasRoleAny
    @GetMapping
    public ResponseEntity<PageResponse<ReviewResponse>> getAll(
            @ModelAttribute ReviewFilter filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAll(filter, pageable));
    }

    @HasRoleAny
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.getById(id));
    }

    @HasRoleUser
    @PostMapping
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(201).body(reviewService.create(request, getCurrentUserId()));
    }

    @HasRoleUser
    @PatchMapping("/{id}/album-id")
    public ResponseEntity<Void> updateAlbumId(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateAlbumIdRequest request) {
        reviewService.updateAlbumId(id, request.getAlbumId());
        return ResponseEntity.noContent().build();
    }

    @HasRoleUser
    @PatchMapping("/{id}/author-id")
    public ResponseEntity<Void> updateAuthorId(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateAuthorIdRequest request) {
        reviewService.updateAuthorId(id, request.getAuthorId());
        return ResponseEntity.noContent().build();
    }

    @HasRoleUser
    @PatchMapping("/{id}/score")
    public ResponseEntity<Void> updateScore(@PathVariable UUID id,
                                            @Valid @RequestBody UpdateScoreRequest request) {
        reviewService.updateScore(id, request.getScore());
        return ResponseEntity.noContent().build();
    }

    @HasRoleUser
    @PatchMapping("/{id}/review-text")
    public ResponseEntity<Void> updateReviewText(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateReviewTextRequest request) {
        reviewService.updateReviewText(id, request.getReviewText());
        return ResponseEntity.noContent().build();
    }

    @HasRoleUser
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
