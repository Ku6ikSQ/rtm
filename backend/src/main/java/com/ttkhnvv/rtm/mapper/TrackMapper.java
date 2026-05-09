package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.track.TrackResponse;
import com.ttkhnvv.rtm.entity.track.Track;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrackMapper {
    TrackResponse toResponse(Track track);
}
