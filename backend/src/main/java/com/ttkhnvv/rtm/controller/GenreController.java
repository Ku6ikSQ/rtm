package com.ttkhnvv.rtm.controller;

import com.ttkhnvv.rtm.dto.pagination.PageResponse;
import com.ttkhnvv.rtm.dto.genre.*;
import com.ttkhnvv.rtm.security.constraint.HasRoleAny;
import com.ttkhnvv.rtm.security.constraint.HasRoleTrusted;
import com.ttkhnvv.rtm.service.GenreService;
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
@RequestMapping(API_PREFIX + "/genres")
public class GenreController {
    private final GenreService genreService;

    @HasRoleAny
    @GetMapping
    public ResponseEntity<PageResponse<GenreResponse>> getAll(
            @ParameterObject @ModelAttribute GenreFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(genreService.getAll(filter, pageable));
    }

    @HasRoleAny
    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(genreService.getById(id));
    }

    @HasRoleTrusted
    @PostMapping
    public ResponseEntity<GenreResponse> create(@Valid @RequestBody CreateGenreRequest request) {
        return ResponseEntity.status(201).body(genreService.create(request));
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/name")
    public ResponseEntity<Void> updateName(@PathVariable UUID id,
                                           @Valid @RequestBody UpdateNameRequest request) {
        genreService.updateName(id, request.getName());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/slug")
    public ResponseEntity<Void> updateSlug(@PathVariable UUID id,
                                           @Valid @RequestBody UpdateSlugRequest request) {
        genreService.updateSlug(id, request.getSlug());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/description")
    public ResponseEntity<Void> updateDescription(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateDescriptionRequest request) {
        genreService.updateDescription(id, request.getDescription());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @PatchMapping("/{id}/parent")
    public ResponseEntity<Void> updateParent(@PathVariable UUID id,
                                             @RequestBody UpdateParentIdRequest request) {
        genreService.updateParent(id, request.getParentId());
        return ResponseEntity.noContent().build();
    }

    @HasRoleTrusted
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        genreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
