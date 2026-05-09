package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.PageResponse;
import com.ttkhnvv.rtm.dto.platform.CreatePlatformRequest;
import com.ttkhnvv.rtm.dto.platform.PlatformFilter;
import com.ttkhnvv.rtm.dto.platform.PlatformResponse;
import com.ttkhnvv.rtm.dto.platform.UpdateNameRequest;
import com.ttkhnvv.rtm.security.constraint.HasRoleAny;
import com.ttkhnvv.rtm.security.constraint.HasRoleTrusted;
import com.ttkhnvv.rtm.service.PlatformService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/platforms")
public class PlatformController {
    private final PlatformService platformService;

    @HasRoleAny
    @GetMapping
    public ResponseEntity<PageResponse<PlatformResponse>> getAll(
            @ModelAttribute PlatformFilter filter,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(platformService.getAll(filter, pageable));
    }

    @HasRoleAny
    @GetMapping("/{id}")
    public ResponseEntity<PlatformResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(platformService.getById(id));
    }

    @HasRoleTrusted
    @PostMapping
    public ResponseEntity<PlatformResponse> create(@Valid @RequestBody CreatePlatformRequest request) {
        return ResponseEntity.status(201).body(platformService.create(request));
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/name")
    public ResponseEntity<Void> updateName(@PathVariable UUID id,
                                           @Valid @RequestBody UpdateNameRequest request) {
        platformService.updateName(id, request.getName());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PutMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateLogo(@PathVariable UUID id,
                                           @RequestParam("file") MultipartFile file) {
        platformService.updateLogo(id, file);
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        platformService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
