package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.albumgenre.AlbumGenreResponse;
import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenre;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlbumGenreMapper {
    AlbumGenreResponse toResponse(AlbumGenre albumGenre);
}
