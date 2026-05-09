package com.ttkhnvv.rtm.dto.album;

import com.ttkhnvv.rtm.validation.album.ValidReleaseYear;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReleaseYearRequest {
    @ValidReleaseYear
    private Integer releaseYear;
}
