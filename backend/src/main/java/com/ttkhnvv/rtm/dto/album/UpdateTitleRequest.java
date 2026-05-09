package com.ttkhnvv.rtm.dto.album;

import com.ttkhnvv.rtm.validation.album.ValidAlbumTitle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTitleRequest {
    @ValidAlbumTitle
    private String title;
}
