package com.ttkhnvv.rtm.dto.review;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAlbumIdRequest {
    @NotNull(message = "Album ID is required")
    private UUID albumId;
}
