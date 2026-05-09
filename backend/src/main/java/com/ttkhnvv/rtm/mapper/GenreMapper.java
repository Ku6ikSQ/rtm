package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.genre.GenreResponse;
import com.ttkhnvv.rtm.entity.genre.Genre;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    GenreResponse toResponse(Genre genre);
}
