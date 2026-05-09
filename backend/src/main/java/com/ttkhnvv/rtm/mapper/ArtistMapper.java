package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.artist.ArtistResponse;
import com.ttkhnvv.rtm.entity.artist.Artist;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArtistMapper {
    ArtistResponse toResponse(Artist artist);
}