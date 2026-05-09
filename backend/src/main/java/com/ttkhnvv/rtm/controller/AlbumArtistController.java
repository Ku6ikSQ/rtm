package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.albumartist.*;
import com.ttkhnvv.rtm.security.constraint.HasRoleAny;
import com.ttkhnvv.rtm.security.constraint.HasRoleTrusted;
import com.ttkhnvv.rtm.service.AlbumArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/album-artists")
public class AlbumArtistController {
    private final AlbumArtistService albumArtistService;

    @HasRoleAny
    @GetMapping("/by-album/{albumId}")
    public ResponseEntity<List<AlbumArtistResponse>> getByAlbumId(@PathVariable UUID albumId) {
        return ResponseEntity.ok(albumArtistService.getByAlbumId(albumId));
    }

    @HasRoleAny
    @GetMapping("/{albumId}/{artistId}")
    public ResponseEntity<AlbumArtistResponse> getById(@PathVariable UUID albumId,
                                                       @PathVariable UUID artistId) {
        return ResponseEntity.ok(albumArtistService.getById(albumId, artistId));
    }

    @HasRoleTrusted
    @PostMapping
    public ResponseEntity<AlbumArtistResponse> create(@Valid @RequestBody CreateAlbumArtistRequest request) {
        return ResponseEntity.status(201).body(albumArtistService.create(request));
    }

    @HasRoleTrusted
    @PatchMapping("/{albumId}/{artistId}/album-id")
    public ResponseEntity<Void> updateAlbumId(@PathVariable UUID albumId,
                                              @PathVariable UUID artistId,
                                              @Valid @RequestBody UpdateAlbumIdRequest request) {
        albumArtistService.updateAlbumId(albumId, artistId, request.getAlbumId());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{albumId}/{artistId}/artist-id")
    public ResponseEntity<Void> updateArtistId(@PathVariable UUID albumId,
                                               @PathVariable UUID artistId,
                                               @Valid @RequestBody UpdateArtistIdRequest request) {
        albumArtistService.updateArtistId(albumId, artistId, request.getArtistId());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{albumId}/{artistId}/role")
    public ResponseEntity<Void> updateRole(@PathVariable UUID albumId,
                                           @PathVariable UUID artistId,
                                           @RequestBody UpdateRoleRequest request) {
        albumArtistService.updateRole(albumId, artistId, request.getRole());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{albumId}/{artistId}/order")
    public ResponseEntity<Void> updateOrder(@PathVariable UUID albumId,
                                            @PathVariable UUID artistId,
                                            @Valid @RequestBody UpdateOrderRequest request) {
        albumArtistService.updateOrder(albumId, artistId, request.getOrder());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @DeleteMapping("/{albumId}/{artistId}")
    public ResponseEntity<Void> delete(@PathVariable UUID albumId,
                                       @PathVariable UUID artistId) {
        albumArtistService.delete(albumId, artistId);
        return ResponseEntity.noContent().build();
    }
}
