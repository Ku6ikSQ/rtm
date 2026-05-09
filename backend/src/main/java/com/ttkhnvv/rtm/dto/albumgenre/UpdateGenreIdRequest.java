package com.ttkhnvv.rtm.dto.albumgenre;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGenreIdRequest {
    @NotNull(message = "Genre ID is required")
    private UUID genreId;
}