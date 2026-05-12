package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.albumlink.AlbumLinkResponse;
import com.ttkhnvv.rtm.dto.albumlink.CreateAlbumLinkRequest;
import com.ttkhnvv.rtm.security.constraint.HasRoleAny;
import com.ttkhnvv.rtm.security.constraint.HasRoleTrusted;
import com.ttkhnvv.rtm.service.AlbumLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/album-links")
public class AlbumLinkController {
    private final AlbumLinkService albumLinkService;

    @HasRoleAny
    @GetMapping("/by-album/{albumId}")
    public ResponseEntity<List<AlbumLinkResponse>> getByAlbumId(@PathVariable UUID albumId) {
        return ResponseEntity.ok(albumLinkService.getByAlbumId(albumId));
    }

    @HasRoleTrusted
    @PostMapping
    public ResponseEntity<AlbumLinkResponse> create(@Valid @RequestBody CreateAlbumLinkRequest request) {
        return ResponseEntity.status(201).body(albumLinkService.create(request));
    }

    @HasRoleTrusted
    @DeleteMapping("/{albumId}/{platformId}")
    public ResponseEntity<Void> delete(@PathVariable UUID albumId, @PathVariable UUID platformId) {
        albumLinkService.delete(albumId, platformId);
        return ResponseEntity.noContent().build();
    }
}