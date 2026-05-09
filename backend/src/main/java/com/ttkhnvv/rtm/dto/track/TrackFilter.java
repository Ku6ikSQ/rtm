package com.ttkhnvv.rtm.dto.track;

import lombok.Data;

import java.util.UUID;

@Data
public class TrackFilter {
    private String title;
    private UUID albumId;
}
