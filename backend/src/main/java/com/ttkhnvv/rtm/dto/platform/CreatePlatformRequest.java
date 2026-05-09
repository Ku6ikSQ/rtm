package com.ttkhnvv.rtm.dto.platform;

import com.ttkhnvv.rtm.validation.platform.ValidPlatformName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePlatformRequest {
    @ValidPlatformName
    private String name;
}
