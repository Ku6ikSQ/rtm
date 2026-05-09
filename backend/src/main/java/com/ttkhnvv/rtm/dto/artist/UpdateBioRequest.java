package com.ttkhnvv.rtm.dto.artist;

import com.ttkhnvv.rtm.validation.artist.ValidBio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBioRequest {
    @ValidBio
    private String bio;
}
