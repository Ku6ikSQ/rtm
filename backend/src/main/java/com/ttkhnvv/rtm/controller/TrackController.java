package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.pagination.PageResponse;
import com.ttkhnvv.rtm.dto.track.*;
import com.ttkhnvv.rtm.security.constraint.HasRoleAny;
import com.ttkhnvv.rtm.security.constraint.HasRoleTrusted;
import com.ttkhnvv.rtm.service.TrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/tracks")
public class TrackController {
    private final TrackService trackService;

    @HasRoleAny
    @GetMapping
    public ResponseEntity<PageResponse<TrackResponse>> getAll(
            @ParameterObject @ModelAttribute TrackFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "trackNumber") Pageable pageable) {
        return ResponseEntity.ok(trackService.getAll(filter, pageable));
    }

    @HasRoleAny
    @GetMapping("/{id}")
    public ResponseEntity<TrackResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(trackService.getById(id));
    }

    @HasRoleTrusted
    @PostMapping
    public ResponseEntity<TrackResponse> create(@Valid @RequestBody CreateTrackRequest request) {
        return ResponseEntity.status(201).body(trackService.create(request));
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/title")
    public ResponseEntity<Void> updateTitle(@PathVariable UUID id,
                                            @Valid @RequestBody UpdateTitleRequest request) {
        trackService.updateTitle(id, request.getTitle());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/album-id")
    public ResponseEntity<Void> updateAlbumId(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateAlbumIdRequest request) {
        trackService.updateAlbumId(id, request.getAlbumId());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/track-number")
    public ResponseEntity<Void> updateTrackNumber(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateTrackNumberRequest request) {
        trackService.updateTrackNumber(id, request.getTrackNumber());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/duration-seconds")
    public ResponseEntity<Void> updateDurationSeconds(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateDurationSecondsRequest request) {
        trackService.updateDurationSeconds(id, request.getDurationSeconds());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        trackService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
