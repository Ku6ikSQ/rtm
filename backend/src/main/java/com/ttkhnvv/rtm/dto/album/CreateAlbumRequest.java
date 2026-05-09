package com.ttkhnvv.rtm.dto.album;

import com.ttkhnvv.rtm.validation.album.ValidAlbumDescription;
import com.ttkhnvv.rtm.validation.album.ValidAlbumTitle;
import com.ttkhnvv.rtm.validation.album.ValidReleaseYear;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAlbumRequest {
    @ValidAlbumTitle
    private String title;
    @ValidAlbumDescription
    private String description;
    @ValidReleaseYear
    private Integer releaseYear;
}
