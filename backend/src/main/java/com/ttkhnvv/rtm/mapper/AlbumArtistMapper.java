package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.albumartist.AlbumArtistResponse;
import com.ttkhnvv.rtm.entity.albumartist.AlbumArtist;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlbumArtistMapper {
    AlbumArtistResponse toResponse(AlbumArtist albumArtist);
}
