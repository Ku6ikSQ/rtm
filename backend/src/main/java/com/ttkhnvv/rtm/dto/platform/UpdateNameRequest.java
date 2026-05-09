package com.ttkhnvv.rtm.dto.platform;

import com.ttkhnvv.rtm.validation.platform.ValidPlatformName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNameRequest {
    @ValidPlatformName
    private String name;
}
