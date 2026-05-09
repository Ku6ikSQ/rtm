package com.ttkhnvv.rtm.dto.review;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAuthorIdRequest {
    @NotNull(message = "Author ID is required")
    private UUID authorId;
}
