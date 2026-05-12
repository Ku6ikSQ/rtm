package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.albumlink.AlbumLinkResponse;
import com.ttkhnvv.rtm.dto.albumlink.CreateAlbumLinkRequest;
import com.ttkhnvv.rtm.entity.albumlink.AlbumLink;
import com.ttkhnvv.rtm.entity.albumlink.AlbumLinkId;
import com.ttkhnvv.rtm.entity.platform.Platform;
import com.ttkhnvv.rtm.exception.albumlink.AlbumLinkAlreadyExistsException;
import com.ttkhnvv.rtm.exception.albumlink.AlbumLinkNotFoundException;
import com.ttkhnvv.rtm.repository.albumlink.AlbumLinkRepository;
import com.ttkhnvv.rtm.repository.platform.PlatformRepository;
import com.ttkhnvv.rtm.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbumLinkServiceTest {

    @Mock
    private AlbumLinkRepository albumLinkRepository;
    @Mock
    private PlatformRepository platformRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private AlbumLinkService albumLinkService;

    private UUID albumId;
    private UUID platformId;
    private AlbumLinkId compositeId;
    private AlbumLink albumLink;
    private Platform platform;

    @BeforeEach
    void init() {
        albumId = UUID.randomUUID();
        platformId = UUID.randomUUID();
        compositeId = new AlbumLinkId(albumId, platformId);
        platform = Platform.builder()
                .id(platformId)
                .name("Spotify")
                .build();
        albumLink = AlbumLink.builder()
                .albumId(albumId)
                .platformId(platformId)
                .url("https://open.spotify.com/album/test")
                .build();
    }

    @Nested
    class GetByAlbumId {
        @Test
        void shouldReturnLinks_withEmbeddedPlatformInfo() {
            // given
            when(albumLinkRepository.findAllByAlbumId(albumId)).thenReturn(List.of(albumLink));
            when(platformRepository.findAllById(List.of(platformId))).thenReturn(List.of(platform));

            // when
            var result = albumLinkService.getByAlbumId(albumId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPlatformName()).isEqualTo("Spotify");
            assertThat(result.get(0).getUrl()).isEqualTo("https://open.spotify.com/album/test");
            verify(storageService, never()).getPresignedUrl(any());
        }

        @Test
        void shouldGeneratePresignedLogoUrl_whenPlatformHasLogo() {
            // given
            platform.setLogoKey("spotify-logo-key");
            when(albumLinkRepository.findAllByAlbumId(albumId)).thenReturn(List.of(albumLink));
            when(platformRepository.findAllById(any())).thenReturn(List.of(platform));
            when(storageService.getPresignedUrl("spotify-logo-key")).thenReturn("http://presigned-url");

            // when
            var result = albumLinkService.getByAlbumId(albumId);

            // then
            assertThat(result.get(0).getPlatformLogoUrl()).isEqualTo("http://presigned-url");
        }

        @Test
        void shouldReturnEmptyList_whenAlbumHasNoLinks() {
            // given
            when(albumLinkRepository.findAllByAlbumId(albumId)).thenReturn(List.of());

            // when
            var result = albumLinkService.getByAlbumId(albumId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class Create {
        @Test
        void shouldCreateLink_whenPairDoesNotExist() {
            // given
            var request = CreateAlbumLinkRequest.builder()
                    .albumId(albumId)
                    .platformId(platformId)
                    .url("https://open.spotify.com/album/test")
                    .build();
            when(albumLinkRepository.existsById(compositeId)).thenReturn(false);
            when(albumLinkRepository.save(any(AlbumLink.class))).thenReturn(albumLink);
            when(platformRepository.findAllById(any())).thenReturn(List.of(platform));

            // when
            var result = albumLinkService.create(request);

            // then
            assertThat(result.getAlbumId()).isEqualTo(albumId);
            assertThat(result.getPlatformId()).isEqualTo(platformId);
            assertThat(result.getPlatformName()).isEqualTo("Spotify");
            verify(albumLinkRepository).save(any(AlbumLink.class));
        }

        @Test
        void shouldThrowException_whenLinkAlreadyExists() {
            // given
            var request = CreateAlbumLinkRequest.builder()
                    .albumId(albumId)
                    .platformId(platformId)
                    .url("https://open.spotify.com/album/test")
                    .build();
            when(albumLinkRepository.existsById(compositeId)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> albumLinkService.create(request))
                    .isInstanceOf(AlbumLinkAlreadyExistsException.class);
            verify(albumLinkRepository, never()).save(any());
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeleteLink_whenItExists() {
            // given
            when(albumLinkRepository.existsById(compositeId)).thenReturn(true);

            // when
            albumLinkService.delete(albumId, platformId);

            // then
            verify(albumLinkRepository).deleteById(compositeId);
        }

        @Test
        void shouldThrowException_whenLinkNotFound() {
            // given
            when(albumLinkRepository.existsById(compositeId)).thenReturn(false);

            // when/then
            assertThatThrownBy(() -> albumLinkService.delete(albumId, platformId))
                    .isInstanceOf(AlbumLinkNotFoundException.class);
            verify(albumLinkRepository, never()).deleteById(any());
        }
    }
}