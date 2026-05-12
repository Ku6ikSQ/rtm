package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.genre.GenreResponse;
import com.ttkhnvv.rtm.entity.genre.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    @Mapping(target = "albumCount", ignore = true)
    GenreResponse toResponse(Genre genre);
}
