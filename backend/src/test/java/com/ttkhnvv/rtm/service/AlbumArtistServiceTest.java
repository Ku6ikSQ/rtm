package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.albumartist.AlbumArtistResponse;
import com.ttkhnvv.rtm.dto.albumartist.CreateAlbumArtistRequest;
import com.ttkhnvv.rtm.entity.albumartist.AlbumArtist;
import com.ttkhnvv.rtm.entity.albumartist.AlbumArtistId;
import com.ttkhnvv.rtm.entity.albumartist.ArtistRole;
import com.ttkhnvv.rtm.exception.albumartist.AlbumArtistAlreadyExistsException;
import com.ttkhnvv.rtm.exception.albumartist.AlbumArtistNotFoundException;
import com.ttkhnvv.rtm.mapper.AlbumArtistMapper;
import com.ttkhnvv.rtm.repository.albumartist.AlbumArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbumArtistServiceTest {

    @Mock
    private AlbumArtistRepository albumArtistRepository;
    @Mock
    private AlbumArtistMapper albumArtistMapper;

    @InjectMocks
    private AlbumArtistService albumArtistService;

    private UUID albumId;
    private UUID artistId;
    private AlbumArtistId compositeId;
    private AlbumArtist albumArtist;
    private AlbumArtistResponse albumArtistResponse;

    @BeforeEach
    void init() {
        albumId = UUID.randomUUID();
        artistId = UUID.randomUUID();
        compositeId = new AlbumArtistId(albumId, artistId);
        albumArtist = AlbumArtist.builder()
                .albumId(albumId)
                .artistId(artistId)
                .role(ArtistRole.MAIN)
                .order(1)
                .build();
        albumArtistResponse = AlbumArtistResponse.builder()
                .albumId(albumId)
                .artistId(artistId)
                .role(ArtistRole.MAIN)
                .order(1)
                .build();
    }

    @Nested
    class GetByAlbumId {
        @Test
        void shouldReturnMappedList_whenAlbumHasArtists() {
            // given
            when(albumArtistRepository.findAllByAlbumId(albumId)).thenReturn(List.of(albumArtist));
            when(albumArtistMapper.toResponse(albumArtist)).thenReturn(albumArtistResponse);

            // when
            var result = albumArtistService.getByAlbumId(albumId);

            // then
            assertThat(result).containsExactly(albumArtistResponse);
        }

        @Test
        void shouldReturnEmptyList_whenAlbumHasNoArtists() {
            // given
            when(albumArtistRepository.findAllByAlbumId(albumId)).thenReturn(List.of());

            // when
            var result = albumArtistService.getByAlbumId(albumId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetById {
        @Test
        void shouldReturnMappedResponse_whenLinkExists() {
            // given
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.of(albumArtist));
            when(albumArtistMapper.toResponse(albumArtist)).thenReturn(albumArtistResponse);

            // when
            var result = albumArtistService.getById(albumId, artistId);

            // then
            assertThat(result).isEqualTo(albumArtistResponse);
        }

        @Test
        void shouldThrowException_whenLinkNotFound() {
            // given
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumArtistService.getById(albumId, artistId))
                    .isInstanceOf(AlbumArtistNotFoundException.class);
        }
    }

    @Nested
    class Create {
        @Test
        void shouldCreateAndReturnLink_whenLinkDoesNotExist() {
            // given
            var request = CreateAlbumArtistRequest.builder()
                    .albumId(albumId)
                    .artistId(artistId)
                    .role(ArtistRole.MAIN)
                    .order(1)
                    .build();
            when(albumArtistRepository.existsById(compositeId)).thenReturn(false);
            when(albumArtistRepository.save(any(AlbumArtist.class))).thenReturn(albumArtist);
            when(albumArtistMapper.toResponse(albumArtist)).thenReturn(albumArtistResponse);

            // when
            var result = albumArtistService.create(request);

            // then
            assertThat(result).isEqualTo(albumArtistResponse);
        }

        @Test
        void shouldThrowException_whenLinkAlreadyExists() {
            // given
            var request = CreateAlbumArtistRequest.builder()
                    .albumId(albumId)
                    .artistId(artistId)
                    .role(ArtistRole.MAIN)
                    .order(1)
                    .build();
            when(albumArtistRepository.existsById(compositeId)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> albumArtistService.create(request))
                    .isInstanceOf(AlbumArtistAlreadyExistsException.class);
        }
    }

    @Nested
    class UpdateAlbumId {
        @Test
        void shouldReCreateLink_whenTargetAlbumIsAvailable() {
            // given
            var newAlbumId = UUID.randomUUID();
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.of(albumArtist));
            when(albumArtistRepository.existsById(new AlbumArtistId(newAlbumId, artistId))).thenReturn(false);

            // when
            albumArtistService.updateAlbumId(albumId, artistId, newAlbumId);

            // then
            verify(albumArtistRepository).deleteById(compositeId);
            verify(albumArtistRepository).save(any(AlbumArtist.class));
        }

        @Test
        void shouldThrowException_whenCurrentLinkNotFound() {
            // given
            var newAlbumId = UUID.randomUUID();
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumArtistService.updateAlbumId(albumId, artistId, newAlbumId))
                    .isInstanceOf(AlbumArtistNotFoundException.class);
        }

        @Test
        void shouldThrowException_whenArtistAlreadyLinkedToTargetAlbum() {
            // given
            var newAlbumId = UUID.randomUUID();
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.of(albumArtist));
            when(albumArtistRepository.existsById(new AlbumArtistId(newAlbumId, artistId))).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> albumArtistService.updateAlbumId(albumId, artistId, newAlbumId))
                    .isInstanceOf(AlbumArtistAlreadyExistsException.class);
        }
    }

    @Nested
    class UpdateArtistId {
        @Test
        void shouldReCreateLink_whenTargetArtistIsAvailable() {
            // given
            var newArtistId = UUID.randomUUID();
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.of(albumArtist));
            when(albumArtistRepository.existsById(new AlbumArtistId(albumId, newArtistId))).thenReturn(false);

            // when
            albumArtistService.updateArtistId(albumId, artistId, newArtistId);

            // then
            verify(albumArtistRepository).deleteById(compositeId);
            verify(albumArtistRepository).save(any(AlbumArtist.class));
        }

        @Test
        void shouldThrowException_whenCurrentLinkNotFound() {
            // given
            var newArtistId = UUID.randomUUID();
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumArtistService.updateArtistId(albumId, artistId, newArtistId))
                    .isInstanceOf(AlbumArtistNotFoundException.class);
        }

        @Test
        void shouldThrowException_whenTargetArtistAlreadyLinkedToAlbum() {
            // given
            var newArtistId = UUID.randomUUID();
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.of(albumArtist));
            when(albumArtistRepository.existsById(new AlbumArtistId(albumId, newArtistId))).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> albumArtistService.updateArtistId(albumId, artistId, newArtistId))
                    .isInstanceOf(AlbumArtistAlreadyExistsException.class);
        }
    }

    @Nested
    class UpdateRole {
        @Test
        void shouldUpdateRole_whenLinkExists() {
            // given
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.of(albumArtist));

            // when
            albumArtistService.updateRole(albumId, artistId, ArtistRole.FEATURED);

            // then
            assertThat(albumArtist.getRole()).isEqualTo(ArtistRole.FEATURED);
            verify(albumArtistRepository).save(albumArtist);
        }

        @Test
        void shouldThrowException_whenLinkNotFound() {
            // given
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumArtistService.updateRole(albumId, artistId, ArtistRole.FEATURED))
                    .isInstanceOf(AlbumArtistNotFoundException.class);
        }
    }

    @Nested
    class UpdateOrder {
        @Test
        void shouldUpdateOrder_whenLinkExists() {
            // given
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.of(albumArtist));

            // when
            albumArtistService.updateOrder(albumId, artistId, 2);

            // then
            assertThat(albumArtist.getOrder()).isEqualTo(2);
            verify(albumArtistRepository).save(albumArtist);
        }

        @Test
        void shouldThrowException_whenLinkNotFound() {
            // given
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumArtistService.updateOrder(albumId, artistId, 2))
                    .isInstanceOf(AlbumArtistNotFoundException.class);
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeleteLink_whenLinkExists() {
            // given
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.of(albumArtist));

            // when
            albumArtistService.delete(albumId, artistId);

            // then
            verify(albumArtistRepository).deleteById(compositeId);
        }

        @Test
        void shouldThrowException_whenLinkNotFound() {
            // given
            when(albumArtistRepository.findById(compositeId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumArtistService.delete(albumId, artistId))
                    .isInstanceOf(AlbumArtistNotFoundException.class);
            verify(albumArtistRepository, never()).deleteById(any());
        }
    }
}
