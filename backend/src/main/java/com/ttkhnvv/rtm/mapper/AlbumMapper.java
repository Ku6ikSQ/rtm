package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.album.AlbumResponse;
import com.ttkhnvv.rtm.entity.album.Album;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlbumMapper {
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "artists", ignore = true)
    AlbumResponse toResponse(Album album);
}
