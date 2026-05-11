package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.artist.ArtistResponse;
import com.ttkhnvv.rtm.dto.artist.CreateArtistRequest;
import com.ttkhnvv.rtm.entity.artist.Artist;
import com.ttkhnvv.rtm.exception.artist.ArtistNotFoundException;
import com.ttkhnvv.rtm.mapper.ArtistMapper;
import com.ttkhnvv.rtm.repository.artist.ArtistRepository;
import com.ttkhnvv.rtm.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private ArtistMapper artistMapper;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private ArtistService artistService;

    private UUID artistId;
    private Artist artist;
    private ArtistResponse artistResponse;

    @BeforeEach
    void init() {
        artistId = UUID.randomUUID();
        artist = Artist.builder()
                .id(artistId)
                .stageName("Stage Name")
                .realName("Real Name")
                .bio("Bio text")
                .country("US")
                .build();
        artistResponse = ArtistResponse.builder()
                .id(artistId)
                .stageName("Stage Name")
                .realName("Real Name")
                .bio("Bio text")
                .country("US")
                .build();
    }

    @Nested
    class GetById {
        @Test
        void shouldReturnArtistResponse_whenArtistHasNoPhoto() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
            when(artistMapper.toResponse(artist)).thenReturn(artistResponse);

            // when
            var result = artistService.getById(artistId);

            // then
            assertThat(result).isEqualTo(artistResponse);
            verify(storageService, never()).getPresignedUrl(any());
        }

        @Test
        void shouldReturnArtistResponseWithPresignedUrl_whenArtistHasPhoto() {
            // given
            artist.setImageKey("image-key");
            artistResponse.setImageUrl("http://presigned-url");
            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
            when(artistMapper.toResponse(artist)).thenReturn(artistResponse);
            when(storageService.getPresignedUrl("image-key")).thenReturn("http://presigned-url");

            // when
            var result = artistService.getById(artistId);

            // then
            assertThat(result.getImageUrl()).isEqualTo("http://presigned-url");
        }

        @Test
        void shouldThrowException_whenArtistNotFound() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> artistService.getById(artistId))
                    .isInstanceOf(ArtistNotFoundException.class);
        }
    }

    @Nested
    class Create {
        @Test
        void shouldCreateAndReturnArtist() {
            // given
            var request = CreateArtistRequest.builder()
                    .stageName("Stage Name")
                    .realName("Real Name")
                    .bio("Bio text")
                    .country("US")
                    .build();
            when(artistRepository.save(any(Artist.class))).thenReturn(artist);
            when(artistMapper.toResponse(artist)).thenReturn(artistResponse);

            // when
            var result = artistService.create(request);

            // then
            assertThat(result).isEqualTo(artistResponse);
            verify(artistRepository).save(any(Artist.class));
        }
    }

    @Nested
    class UpdateStageName {
        @Test
        void shouldUpdateStageName_whenArtistExists() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

            // when
            artistService.updateStageName(artistId, "New Stage");

            // then
            assertThat(artist.getStageName()).isEqualTo("New Stage");
            verify(artistRepository).save(artist);
        }

        @Test
        void shouldThrowException_whenArtistNotFound() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> artistService.updateStageName(artistId, "New Stage"))
                    .isInstanceOf(ArtistNotFoundException.class);
        }
    }

    @Nested
    class UpdateRealName {
        @Test
        void shouldUpdateRealName_whenArtistExists() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

            // when
            artistService.updateRealName(artistId, "New Real Name");

            // then
            assertThat(artist.getRealName()).isEqualTo("New Real Name");
            verify(artistRepository).save(artist);
        }

        @Test
        void shouldThrowException_whenArtistNotFound() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> artistService.updateRealName(artistId, "New Real Name"))
                    .isInstanceOf(ArtistNotFoundException.class);
        }
    }

    @Nested
    class UpdateBio {
        @Test
        void shouldUpdateBio_whenArtistExists() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

            // when
            artistService.updateBio(artistId, "New bio");

            // then
            assertThat(artist.getBio()).isEqualTo("New bio");
            verify(artistRepository).save(artist);
        }

        @Test
        void shouldThrowException_whenArtistNotFound() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> artistService.updateBio(artistId, "New bio"))
                    .isInstanceOf(ArtistNotFoundException.class);
        }
    }

    @Nested
    class UpdateCountry {
        @Test
        void shouldUpdateCountry_whenArtistExists() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

            // when
            artistService.updateCountry(artistId, "UK");

            // then
            assertThat(artist.getCountry()).isEqualTo("UK");
            verify(artistRepository).save(artist);
        }

        @Test
        void shouldThrowException_whenArtistNotFound() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> artistService.updateCountry(artistId, "UK"))
                    .isInstanceOf(ArtistNotFoundException.class);
        }
    }

    @Nested
    class UpdatePhoto {
        @Test
        void shouldUploadNewPhoto_whenArtistHasNoPreviousPhoto() {
            // given
            var file = mock(MultipartFile.class);
            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
            when(storageService.upload(file)).thenReturn("new-key");

            // when
            artistService.updatePhoto(artistId, file);

            // then
            verify(storageService, never()).delete(any());
            assertThat(artist.getImageKey()).isEqualTo("new-key");
            verify(artistRepository).save(artist);
        }

        @Test
        void shouldDeleteOldPhotoAndUploadNew_whenArtistHasExistingPhoto() {
            // given
            artist.setImageKey("old-key");
            var file = mock(MultipartFile.class);
            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
            when(storageService.upload(file)).thenReturn("new-key");

            // when
            artistService.updatePhoto(artistId, file);

            // then
            verify(storageService).delete("old-key");
            assertThat(artist.getImageKey()).isEqualTo("new-key");
        }

        @Test
        void shouldThrowException_whenArtistNotFound() {
            // given
            var file = mock(MultipartFile.class);
            when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> artistService.updatePhoto(artistId, file))
                    .isInstanceOf(ArtistNotFoundException.class);
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeleteArtist_whenArtistHasNoPhoto() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

            // when
            artistService.delete(artistId);

            // then
            verify(storageService, never()).delete(any());
            verify(artistRepository).deleteById(artistId);
        }

        @Test
        void shouldDeletePhotoAndArtist_whenArtistHasPhoto() {
            // given
            artist.setImageKey("image-key");
            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

            // when
            artistService.delete(artistId);

            // then
            verify(storageService).delete("image-key");
            verify(artistRepository).deleteById(artistId);
        }

        @Test
        void shouldThrowException_whenArtistNotFound() {
            // given
            when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> artistService.delete(artistId))
                    .isInstanceOf(ArtistNotFoundException.class);
        }
    }
}
