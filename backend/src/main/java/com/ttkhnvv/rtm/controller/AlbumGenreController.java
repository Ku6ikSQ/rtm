package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.albumgenre.*;
import com.ttkhnvv.rtm.service.AlbumGenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/album-genres")
public class AlbumGenreController {
    private final AlbumGenreService albumGenreService;

    @GetMapping("/by-album/{albumId}")
    public ResponseEntity<List<AlbumGenreResponse>> getByAlbumId(@PathVariable UUID albumId) {
        return ResponseEntity.ok(albumGenreService.getByAlbumId(albumId));
    }

    @GetMapping("/{albumId}/{genreId}")
    public ResponseEntity<AlbumGenreResponse> getById(@PathVariable UUID albumId,
                                                      @PathVariable UUID genreId) {
        return ResponseEntity.ok(albumGenreService.getById(albumId, genreId));
    }

    @PostMapping
    public ResponseEntity<AlbumGenreResponse> create(@Valid @RequestBody CreateAlbumGenreRequest request) {
        return ResponseEntity.status(201).body(albumGenreService.create(request));
    }

    @PatchMapping("/{albumId}/{genreId}/album-id")
    public ResponseEntity<Void> updateAlbumId(@PathVariable UUID albumId,
                                              @PathVariable UUID genreId,
                                              @Valid @RequestBody UpdateAlbumIdRequest request) {
        albumGenreService.updateAlbumId(albumId, genreId, request.getAlbumId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{albumId}/{genreId}/genre-id")
    public ResponseEntity<Void> updateGenreId(@PathVariable UUID albumId,
                                              @PathVariable UUID genreId,
                                              @Valid @RequestBody UpdateGenreIdRequest request) {
        albumGenreService.updateGenreId(albumId, genreId, request.getGenreId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{albumId}/{genreId}")
    public ResponseEntity<Void> delete(@PathVariable UUID albumId,
                                       @PathVariable UUID genreId) {
        albumGenreService.delete(albumId, genreId);
        return ResponseEntity.noContent().build();
    }
}
