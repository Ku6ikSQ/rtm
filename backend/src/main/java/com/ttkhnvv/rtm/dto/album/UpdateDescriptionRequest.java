package com.ttkhnvv.rtm.dto.album;

import com.ttkhnvv.rtm.validation.album.ValidAlbumDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDescriptionRequest {
    @ValidAlbumDescription
    private String description;
}
