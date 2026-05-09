package com.ttkhnvv.rtm.dto.genre;

import com.ttkhnvv.rtm.validation.genre.ValidGenreSlug;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSlugRequest {
    @ValidGenreSlug
    private String slug;
}
