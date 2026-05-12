package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.artist.*;
import com.ttkhnvv.rtm.dto.pagination.PageResponse;
import com.ttkhnvv.rtm.security.constraint.HasRoleAny;
import com.ttkhnvv.rtm.security.constraint.HasRoleTrusted;
import com.ttkhnvv.rtm.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/artists")
public class ArtistController {
    private final ArtistService artistService;

    @HasRoleAny
    @GetMapping
    public ResponseEntity<PageResponse<ArtistResponse>> getAll(
            @ParameterObject @ModelAttribute ArtistFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "stageName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(artistService.getAll(filter, pageable));
    }

    @HasRoleAny
    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(artistService.getById(id));
    }

    @HasRoleAny
    @PostMapping
    public ResponseEntity<ArtistResponse> create(@Valid @RequestBody CreateArtistRequest request) {
        return ResponseEntity.status(201).body(artistService.create(request));
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/stage-name")
    public ResponseEntity<Void> updateStageName(@PathVariable UUID id,
                                                @Valid @RequestBody UpdateStageNameRequest request) {
        artistService.updateStageName(id, request.getStageName());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/real-name")
    public ResponseEntity<Void> updateRealName(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateRealNameRequest request) {
        artistService.updateRealName(id, request.getRealName());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/bio")
    public ResponseEntity<Void> updateBio(@PathVariable UUID id,
                                          @Valid @RequestBody UpdateBioRequest request) {
        artistService.updateBio(id, request.getBio());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/country")
    public ResponseEntity<Void> updateCountry(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateCountryRequest request) {
        artistService.updateCountry(id, request.getCountry());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePhoto(@PathVariable UUID id,
                                            @RequestParam("file") MultipartFile file) {
        artistService.updatePhoto(id, file);
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        artistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}