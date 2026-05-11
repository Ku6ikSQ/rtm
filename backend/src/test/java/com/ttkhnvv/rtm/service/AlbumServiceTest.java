package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.album.AlbumFilter;
import com.ttkhnvv.rtm.dto.album.AlbumResponse;
import com.ttkhnvv.rtm.dto.album.CreateAlbumRequest;
import com.ttkhnvv.rtm.entity.album.Album;
import com.ttkhnvv.rtm.entity.track.Track;
import com.ttkhnvv.rtm.exception.album.AlbumNotFoundException;
import com.ttkhnvv.rtm.mapper.AlbumMapper;
import com.ttkhnvv.rtm.repository.album.AlbumRepository;
import com.ttkhnvv.rtm.repository.review.ReviewRepository;
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

import java.math.BigDecimal;
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
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private AlbumMapper albumMapper;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private AlbumService albumService;

    private UUID albumId;
    private Album album;
    private AlbumResponse albumResponse;

    @BeforeEach
    void init() {
        albumId = UUID.randomUUID();
        album = Album.builder()
                .id(albumId)
                .title("Test Album")
                .description("Description")
                .releaseYear(2020)
                .build();
        albumResponse = AlbumResponse.builder()
                .id(albumId)
                .title("Test Album")
                .description("Description")
                .releaseYear(2020)
                .build();
    }

    @Nested
    class GetAll {
        @Test
        void shouldReturnPageOfAlbums_whenFilterMatches() {
            // given
            var filter = AlbumFilter.builder().title("Test").build();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(album));
            when(albumRepository.findAll((Specification<Album>) any(), any(Pageable.class))).thenReturn(page);
            when(albumMapper.toResponse(album)).thenReturn(albumResponse);

            // when
            var result = albumService.getAll(filter, pageable);

            // then
            assertThat(result.getContent()).containsExactly(albumResponse);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        void shouldReturnEmptyPage_whenNoAlbumsMatch() {
            // given
            var filter = AlbumFilter.builder().build();
            var pageable = Pageable.unpaged();
            when(albumRepository.findAll((Specification<Album>) any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            // when
            var result = albumService.getAll(filter, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    class GetById {
        @Test
        void shouldReturnAlbumResponse_whenAlbumExistsWithNoCover() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
            when(albumMapper.toResponse(album)).thenReturn(albumResponse);

            // when
            var result = albumService.getById(albumId);

            // then
            assertThat(result).isEqualTo(albumResponse);
            verify(storageService, never()).getPresignedUrl(any());
        }

        @Test
        void shouldReturnAlbumResponseWithPresignedUrl_whenAlbumHasCover() {
            // given
            album.setCoverKey("cover-key");
            albumResponse.setCoverUrl("http://presigned-url");
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
            when(albumMapper.toResponse(album)).thenReturn(albumResponse);
            when(storageService.getPresignedUrl("cover-key")).thenReturn("http://presigned-url");

            // when
            var result = albumService.getById(albumId);

            // then
            assertThat(result.getCoverUrl()).isEqualTo("http://presigned-url");
        }

        @Test
        void shouldThrowException_whenAlbumNotFound() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumService.getById(albumId))
                    .isInstanceOf(AlbumNotFoundException.class);
        }
    }

    @Nested
    class Create {
        @Test
        void shouldCreateAndReturnAlbum() {
            // given
            var createdBy = UUID.randomUUID();
            var request = CreateAlbumRequest.builder()
                    .title("New Album")
                    .description("Desc")
                    .releaseYear(2023)
                    .build();
            when(albumRepository.save(any(Album.class))).thenReturn(album);
            when(albumMapper.toResponse(album)).thenReturn(albumResponse);

            // when
            var result = albumService.create(request, createdBy);

            // then
            assertThat(result).isEqualTo(albumResponse);
            verify(albumRepository).save(any(Album.class));
        }
    }

    @Nested
    class UpdateTitle {
        @Test
        void shouldUpdateTitle_whenAlbumExists() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

            // when
            albumService.updateTitle(albumId, "Updated Title");

            // then
            assertThat(album.getTitle()).isEqualTo("Updated Title");
            verify(albumRepository).save(album);
        }

        @Test
        void shouldThrowException_whenAlbumNotFound() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumService.updateTitle(albumId, "Updated Title"))
                    .isInstanceOf(AlbumNotFoundException.class);
        }
    }

    @Nested
    class UpdateDescription {
        @Test
        void shouldUpdateDescription_whenAlbumExists() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

            // when
            albumService.updateDescription(albumId, "New description");

            // then
            assertThat(album.getDescription()).isEqualTo("New description");
            verify(albumRepository).save(album);
        }

        @Test
        void shouldThrowException_whenAlbumNotFound() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumService.updateDescription(albumId, "New description"))
                    .isInstanceOf(AlbumNotFoundException.class);
        }
    }

    @Nested
    class UpdateReleaseYear {
        @Test
        void shouldUpdateReleaseYear_whenAlbumExists() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

            // when
            albumService.updateReleaseYear(albumId, 2024);

            // then
            assertThat(album.getReleaseYear()).isEqualTo(2024);
            verify(albumRepository).save(album);
        }

        @Test
        void shouldThrowException_whenAlbumNotFound() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumService.updateReleaseYear(albumId, 2024))
                    .isInstanceOf(AlbumNotFoundException.class);
        }
    }

    @Nested
    class UpdateCover {
        @Test
        void shouldUploadNewCover_whenAlbumHasNoPreviousCover() {
            // given
            var file = mock(MultipartFile.class);
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
            when(storageService.upload(file)).thenReturn("new-key");

            // when
            albumService.updateCover(albumId, file);

            // then
            verify(storageService, never()).delete(any());
            assertThat(album.getCoverKey()).isEqualTo("new-key");
            verify(albumRepository).save(album);
        }

        @Test
        void shouldDeleteOldCoverAndUploadNew_whenAlbumHasExistingCover() {
            // given
            album.setCoverKey("old-key");
            var file = mock(MultipartFile.class);
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
            when(storageService.upload(file)).thenReturn("new-key");

            // when
            albumService.updateCover(albumId, file);

            // then
            verify(storageService).delete("old-key");
            assertThat(album.getCoverKey()).isEqualTo("new-key");
        }

        @Test
        void shouldThrowException_whenAlbumNotFound() {
            // given
            var file = mock(MultipartFile.class);
            when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumService.updateCover(albumId, file))
                    .isInstanceOf(AlbumNotFoundException.class);
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeleteAlbum_whenAlbumHasNoCover() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

            // when
            albumService.delete(albumId);

            // then
            verify(storageService, never()).delete(any());
            verify(albumRepository).deleteById(albumId);
        }

        @Test
        void shouldDeleteCoverAndAlbum_whenAlbumHasCover() {
            // given
            album.setCoverKey("cover-key");
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

            // when
            albumService.delete(albumId);

            // then
            verify(storageService).delete("cover-key");
            verify(albumRepository).deleteById(albumId);
        }

        @Test
        void shouldThrowException_whenAlbumNotFound() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumService.delete(albumId))
                    .isInstanceOf(AlbumNotFoundException.class);
        }
    }

    @Nested
    class RecalculateRating {
        @Test
        void shouldSetAvgRating_whenReviewsExist() {
            // given
            var rating = BigDecimal.valueOf(8.5);
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
            when(reviewRepository.calculateAvgRating(albumId)).thenReturn(Optional.of(rating));

            // when
            albumService.recalculateRating(albumId);

            // then
            assertThat(album.getAvgRating()).isEqualByComparingTo(rating);
            verify(albumRepository).save(album);
        }

        @Test
        void shouldSetNullRating_whenNoReviewsExist() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
            when(reviewRepository.calculateAvgRating(albumId)).thenReturn(Optional.empty());

            // when
            albumService.recalculateRating(albumId);

            // then
            assertThat(album.getAvgRating()).isNull();
            verify(albumRepository).save(album);
        }

        @Test
        void shouldThrowException_whenAlbumNotFound() {
            // given
            when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumService.recalculateRating(albumId))
                    .isInstanceOf(AlbumNotFoundException.class);
        }
    }
}
