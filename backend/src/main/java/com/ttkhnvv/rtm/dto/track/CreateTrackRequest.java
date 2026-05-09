package com.ttkhnvv.rtm.dto.track;

import com.ttkhnvv.rtm.validation.track.ValidDurationSeconds;
import com.ttkhnvv.rtm.validation.track.ValidTrackNumber;
import com.ttkhnvv.rtm.validation.track.ValidTrackTitle;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTrackRequest {
    @ValidTrackTitle
    private String title;
    @NotNull(message = "Album ID is required")
    private UUID albumId;
    @ValidTrackNumber
    private Integer trackNumber;
    @ValidDurationSeconds
    private Integer durationSeconds;
}
