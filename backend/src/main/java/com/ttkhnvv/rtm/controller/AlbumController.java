package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.album.*;
import com.ttkhnvv.rtm.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;
import static com.ttkhnvv.rtm.security.util.SecurityUtils.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/albums")
public class AlbumController {
    private final AlbumService albumService;

    @GetMapping
    public ResponseEntity<List<AlbumResponse>> getAll() {
        return ResponseEntity.ok(albumService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(albumService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AlbumResponse> create(@Valid @RequestBody CreateAlbumRequest request) {
        return ResponseEntity.status(201).body(albumService.create(request, getCurrentUserId()));
    }

    @PatchMapping("/{id}/title")
    public ResponseEntity<Void> updateTitle(@PathVariable UUID id,
                                            @Valid @RequestBody UpdateTitleRequest request) {
        albumService.updateTitle(id, request.getTitle());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/description")
    public ResponseEntity<Void> updateDescription(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateDescriptionRequest request) {
        albumService.updateDescription(id, request.getDescription());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/release-year")
    public ResponseEntity<Void> updateReleaseYear(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateReleaseYearRequest request) {
        albumService.updateReleaseYear(id, request.getReleaseYear());
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateCover(@PathVariable UUID id,
                                            @RequestParam("file") MultipartFile file) {
        albumService.updateCover(id, file);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
