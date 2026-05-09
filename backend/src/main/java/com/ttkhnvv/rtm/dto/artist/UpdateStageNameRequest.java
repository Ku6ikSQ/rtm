package com.ttkhnvv.rtm.dto.artist;

import com.ttkhnvv.rtm.validation.artist.ValidStageName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStageNameRequest {
    @ValidStageName
    private String stageName;
}