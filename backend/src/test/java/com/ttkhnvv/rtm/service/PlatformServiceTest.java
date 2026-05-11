package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.platform.CreatePlatformRequest;
import com.ttkhnvv.rtm.dto.platform.PlatformFilter;
import com.ttkhnvv.rtm.dto.platform.PlatformResponse;
import com.ttkhnvv.rtm.entity.platform.Platform;
import com.ttkhnvv.rtm.exception.platform.PlatformNotFoundException;
import com.ttkhnvv.rtm.mapper.PlatformMapper;
import com.ttkhnvv.rtm.repository.platform.PlatformRepository;
import com.ttkhnvv.rtm.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformServiceTest {

    @Mock
    private PlatformRepository platformRepository;
    @Mock
    private PlatformMapper platformMapper;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private PlatformService platformService;

    private UUID platformId;
    private Platform platform;
    private PlatformResponse platformResponse;

    @BeforeEach
    void init() {
        platformId = UUID.randomUUID();
        platform = Platform.builder()
                .id(platformId)
                .name("Spotify")
                .build();
        platformResponse = PlatformResponse.builder()
                .id(platformId)
                .name("Spotify")
                .build();
    }

    @Nested
    class GetAll {
        @Test
        void shouldReturnPageOfPlatforms_whenFilterMatches() {
            // given
            var filter = new PlatformFilter();
            filter.setName("Spotify");
            var pageable = Pageable.unpaged();
            when(platformRepository.findAll((Specification<Platform>) any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(platform)));
            when(platformMapper.toResponse(platform)).thenReturn(platformResponse);

            // when
            var result = platformService.getAll(filter, pageable);

            // then
            assertThat(result.getContent()).containsExactly(platformResponse);
        }

        @Test
        void shouldReturnEmptyPage_whenNoPlatformsMatch() {
            // given
            var filter = new PlatformFilter();
            var pageable = Pageable.unpaged();
            when(platformRepository.findAll((Specification<Platform>) any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            // when
            var result = platformService.getAll(filter, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    class GetById {
        @Test
        void shouldReturnPlatformResponse_whenPlatformHasNoLogo() {
            // given
            when(platformRepository.findById(platformId)).thenReturn(Optional.of(platform));
            when(platformMapper.toResponse(platform)).thenReturn(platformResponse);

            // when
            var result = platformService.getById(platformId);

            // then
            assertThat(result).isEqualTo(platformResponse);
            verify(storageService, never()).getPresignedUrl(any());
        }

        @Test
        void shouldReturnPlatformResponseWithPresignedUrl_whenPlatformHasLogo() {
            // given
            platform.setLogoKey("logo-key");
            platformResponse.setLogoUrl("http://presigned-url");
            when(platformRepository.findById(platformId)).thenReturn(Optional.of(platform));
            when(platformMapper.toResponse(platform)).thenReturn(platformResponse);
            when(storageService.getPresignedUrl("logo-key")).thenReturn("http://presigned-url");

            // when
            var result = platformService.getById(platformId);

            // then
            assertThat(result.getLogoUrl()).isEqualTo("http://presigned-url");
        }

        @Test
        void shouldThrowException_whenPlatformNotFound() {
            // given
            when(platformRepository.findById(platformId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> platformService.getById(platformId))
                    .isInstanceOf(PlatformNotFoundException.class);
        }
    }

    @Nested
    class Create {
        @Test
        void shouldCreateAndReturnPlatform() {
            // given
            var request = CreatePlatformRequest.builder()
                    .name("Spotify")
                    .build();
            when(platformRepository.save(any(Platform.class))).thenReturn(platform);
            when(platformMapper.toResponse(platform)).thenReturn(platformResponse);

            // when
            var result = platformService.create(request);

            // then
            assertThat(result).isEqualTo(platformResponse);
            verify(platformRepository).save(any(Platform.class));
        }
    }

    @Nested
    class UpdateName {
        @Test
        void shouldUpdateName_whenPlatformExists() {
            // given
            when(platformRepository.findById(platformId)).thenReturn(Optional.of(platform));

            // when
            platformService.updateName(platformId, "Apple Music");

            // then
            assertThat(platform.getName()).isEqualTo("Apple Music");
            verify(platformRepository).save(platform);
        }

        @Test
        void shouldThrowException_whenPlatformNotFound() {
            // given
            when(platformRepository.findById(platformId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> platformService.updateName(platformId, "Apple Music"))
                    .isInstanceOf(PlatformNotFoundException.class);
        }
    }

    @Nested
    class UpdateLogo {
        @Test
        void shouldUploadNewLogo_whenPlatformHasNoPreviousLogo() {
            // given
            var file = mock(MultipartFile.class);
            when(platformRepository.findById(platformId)).thenReturn(Optional.of(platform));
            when(storageService.upload(file)).thenReturn("new-key");

            // when
            platformService.updateLogo(platformId, file);

            // then
            verify(storageService, never()).delete(any());
            assertThat(platform.getLogoKey()).isEqualTo("new-key");
            verify(platformRepository).save(platform);
        }

        @Test
        void shouldDeleteOldLogoAndUploadNew_whenPlatformHasExistingLogo() {
            // given
            platform.setLogoKey("old-key");
            var file = mock(MultipartFile.class);
            when(platformRepository.findById(platformId)).thenReturn(Optional.of(platform));
            when(storageService.upload(file)).thenReturn("new-key");

            // when
            platformService.updateLogo(platformId, file);

            // then
            verify(storageService).delete("old-key");
            assertThat(platform.getLogoKey()).isEqualTo("new-key");
        }

        @Test
        void shouldThrowException_whenPlatformNotFound() {
            // given
            var file = mock(MultipartFile.class);
            when(platformRepository.findById(platformId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> platformService.updateLogo(platformId, file))
                    .isInstanceOf(PlatformNotFoundException.class);
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeletePlatform_whenPlatformHasNoLogo() {
            // given
            when(platformRepository.findById(platformId)).thenReturn(Optional.of(platform));

            // when
            platformService.delete(platformId);

            // then
            verify(storageService, never()).delete(any());
            verify(platformRepository).deleteById(platformId);
        }

        @Test
        void shouldDeleteLogoAndPlatform_whenPlatformHasLogo() {
            // given
            platform.setLogoKey("logo-key");
            when(platformRepository.findById(platformId)).thenReturn(Optional.of(platform));

            // when
            platformService.delete(platformId);

            // then
            verify(storageService).delete("logo-key");
            verify(platformRepository).deleteById(platformId);
        }

        @Test
        void shouldThrowException_whenPlatformNotFound() {
            // given
            when(platformRepository.findById(platformId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> platformService.delete(platformId))
                    .isInstanceOf(PlatformNotFoundException.class);
        }
    }
}
