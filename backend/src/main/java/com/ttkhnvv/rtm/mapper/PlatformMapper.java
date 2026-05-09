package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.platform.PlatformResponse;
import com.ttkhnvv.rtm.entity.platform.Platform;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlatformMapper {
    @Mapping(target = "logoUrl", ignore = true)
    PlatformResponse toResponse(Platform platform);
}
