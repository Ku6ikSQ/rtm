package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.album.AlbumResponse;
import com.ttkhnvv.rtm.entity.album.Album;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-11T11:25:47+0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class AlbumMapperImpl implements AlbumMapper {

    @Override
    public AlbumResponse toResponse(Album album) {
        if ( album == null ) {
            return null;
        }

        AlbumResponse.AlbumResponseBuilder albumResponse = AlbumResponse.builder();

        albumResponse.id( album.getId() );
        albumResponse.title( album.getTitle() );
        albumResponse.description( album.getDescription() );
        albumResponse.releaseYear( album.getReleaseYear() );
        albumResponse.avgRating( album.getAvgRating() );
        albumResponse.createdAt( album.getCreatedAt() );
        albumResponse.createdBy( album.getCreatedBy() );

        return albumResponse.build();
    }
}
