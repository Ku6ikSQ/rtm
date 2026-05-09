package com.ttkhnvv.rtm.dto.track;

import com.ttkhnvv.rtm.validation.track.ValidTrackNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTrackNumberRequest {
    @ValidTrackNumber
    private Integer trackNumber;
}
