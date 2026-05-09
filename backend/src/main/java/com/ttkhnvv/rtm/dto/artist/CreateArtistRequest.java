package com.ttkhnvv.rtm.dto.artist;

import com.ttkhnvv.rtm.validation.artist.ValidBio;
import com.ttkhnvv.rtm.validation.artist.ValidCountry;
import com.ttkhnvv.rtm.validation.artist.ValidRealName;
import com.ttkhnvv.rtm.validation.artist.ValidStageName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateArtistRequest {
    @ValidStageName
    private String stageName;
    @ValidRealName
    private String realName;
    @ValidBio
    private String bio;
    @ValidCountry
    private String country;
}
