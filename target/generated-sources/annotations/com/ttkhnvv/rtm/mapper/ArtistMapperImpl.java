package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.artist.ArtistResponse;
import com.ttkhnvv.rtm.entity.artist.Artist;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-09T12:58:16+0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class ArtistMapperImpl implements ArtistMapper {

    @Override
    public ArtistResponse toResponse(Artist artist) {
        if ( artist == null ) {
            return null;
        }

        ArtistResponse.ArtistResponseBuilder artistResponse = ArtistResponse.builder();

        artistResponse.id( artist.getId() );
        artistResponse.stageName( artist.getStageName() );
        artistResponse.realName( artist.getRealName() );
        artistResponse.bio( artist.getBio() );
        artistResponse.country( artist.getCountry() );
        artistResponse.createdAt( artist.getCreatedAt() );

        return artistResponse.build();
    }
}
