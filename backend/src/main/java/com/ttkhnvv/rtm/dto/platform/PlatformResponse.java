package com.ttkhnvv.rtm.dto.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformResponse {
    private UUID id;
    private String name;
    private String logoUrl;
}
