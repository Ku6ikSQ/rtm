package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.platform.CreatePlatformRequest;
import com.ttkhnvv.rtm.dto.platform.PlatformResponse;
import com.ttkhnvv.rtm.dto.platform.UpdateNameRequest;
import com.ttkhnvv.rtm.service.PlatformService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.ttkhnvv.rtm.config.ApiConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/platforms")
public class PlatformController {
    private final PlatformService platformService;

    @GetMapping
    public ResponseEntity<List<PlatformResponse>> getAll() {
        return ResponseEntity.ok(platformService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlatformResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(platformService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PlatformResponse> create(@Valid @RequestBody CreatePlatformRequest request) {
        return ResponseEntity.status(201).body(platformService.create(request));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<Void> updateName(@PathVariable UUID id,
                                           @Valid @RequestBody UpdateNameRequest request) {
        platformService.updateName(id, request.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateLogo(@PathVariable UUID id,
                                           @RequestParam("file") MultipartFile file) {
        platformService.updateLogo(id, file);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        platformService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
