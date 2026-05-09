package com.ttkhnvv.rtm.dto.track;

import com.ttkhnvv.rtm.validation.track.ValidDurationSeconds;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDurationSecondsRequest {
    @ValidDurationSeconds
    private Integer durationSeconds;
}
