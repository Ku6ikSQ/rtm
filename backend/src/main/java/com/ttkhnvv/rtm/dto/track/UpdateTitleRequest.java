package com.ttkhnvv.rtm.dto.track;

import com.ttkhnvv.rtm.validation.track.ValidTrackTitle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTitleRequest {
    @ValidTrackTitle
    private String title;
}
