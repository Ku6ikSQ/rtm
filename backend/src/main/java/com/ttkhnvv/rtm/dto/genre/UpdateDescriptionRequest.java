package com.ttkhnvv.rtm.dto.genre;

import com.ttkhnvv.rtm.validation.genre.ValidGenreDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDescriptionRequest {
    @ValidGenreDescription
    private String description;
}
