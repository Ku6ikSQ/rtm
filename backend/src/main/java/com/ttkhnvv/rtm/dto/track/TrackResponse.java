package com.ttkhnvv.rtm.dto.track;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackResponse {
    private UUID id;
    private String title;
    private UUID albumId;
    private Integer trackNumber;
    private Integer durationSeconds;
}
