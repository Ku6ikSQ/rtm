package com.ttkhnvv.rtm.dto.genre;

import com.ttkhnvv.rtm.validation.genre.ValidGenreDescription;
import com.ttkhnvv.rtm.validation.genre.ValidGenreName;
import com.ttkhnvv.rtm.validation.genre.ValidGenreSlug;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateGenreRequest {
    @ValidGenreName
    private String name;
    @ValidGenreSlug
    private String slug;
    @ValidGenreDescription
    private String description;
    private UUID parentId;
}
