package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.albumgenre.AlbumGenreResponse;
import com.ttkhnvv.rtm.dto.albumgenre.CreateAlbumGenreRequest;
import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenre;
import com.ttkhnvv.rtm.entity.albumgenre.AlbumGenreId;
import com.ttkhnvv.rtm.exception.albumgenre.AlbumGenreAlreadyExistsException;
import com.ttkhnvv.rtm.exception.albumgenre.AlbumGenreNotFoundException;
import com.ttkhnvv.rtm.mapper.AlbumGenreMapper;
import com.ttkhnvv.rtm.repository.albumgenre.AlbumGenreRepository;
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
class AlbumGenreServiceTest {

    @Mock
    private AlbumGenreRepository albumGenreRepository;
    @Mock
    private AlbumGenreMapper albumGenreMapper;

    @InjectMocks
    private AlbumGenreService albumGenreService;

    private UUID albumId;
    private UUID genreId;
    private AlbumGenreId compositeId;
    private AlbumGenre albumGenre;
    private AlbumGenreResponse albumGenreResponse;

    @BeforeEach
    void init() {
        albumId = UUID.randomUUID();
        genreId = UUID.randomUUID();
        compositeId = new AlbumGenreId(albumId, genreId);
        albumGenre = AlbumGenre.builder()
                .albumId(albumId)
                .genreId(genreId)
                .build();
        albumGenreResponse = AlbumGenreResponse.builder()
                .albumId(albumId)
                .genreId(genreId)
                .build();
    }

    @Nested
    class GetByAlbumId {
        @Test
        void shouldReturnMappedList_whenAlbumHasGenres() {
            // given
            when(albumGenreRepository.findAllByAlbumId(albumId)).thenReturn(List.of(albumGenre));
            when(albumGenreMapper.toResponse(albumGenre)).thenReturn(albumGenreResponse);

            // when
            var result = albumGenreService.getByAlbumId(albumId);

            // then
            assertThat(result).containsExactly(albumGenreResponse);
        }

        @Test
        void shouldReturnEmptyList_whenAlbumHasNoGenres() {
            // given
            when(albumGenreRepository.findAllByAlbumId(albumId)).thenReturn(List.of());

            // when
            var result = albumGenreService.getByAlbumId(albumId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetById {
        @Test
        void shouldReturnMappedResponse_whenLinkExists() {
            // given
            when(albumGenreRepository.findById(compositeId)).thenReturn(Optional.of(albumGenre));
            when(albumGenreMapper.toResponse(albumGenre)).thenReturn(albumGenreResponse);

            // when
            var result = albumGenreService.getById(albumId, genreId);

            // then
            assertThat(result).isEqualTo(albumGenreResponse);
        }

        @Test
        void shouldThrowException_whenLinkNotFound() {
            // given
            when(albumGenreRepository.findById(compositeId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumGenreService.getById(albumId, genreId))
                    .isInstanceOf(AlbumGenreNotFoundException.class);
        }
    }

    @Nested
    class Create {
        @Test
        void shouldCreateAndReturnLink_whenLinkDoesNotExist() {
            // given
            var request = CreateAlbumGenreRequest.builder()
                    .albumId(albumId)
                    .genreId(genreId)
                    .build();
            when(albumGenreRepository.existsById(compositeId)).thenReturn(false);
            when(albumGenreRepository.save(any(AlbumGenre.class))).thenReturn(albumGenre);
            when(albumGenreMapper.toResponse(albumGenre)).thenReturn(albumGenreResponse);

            // when
            var result = albumGenreService.create(request);

            // then
            assertThat(result).isEqualTo(albumGenreResponse);
        }

        @Test
        void shouldThrowException_whenLinkAlreadyExists() {
            // given
            var request = CreateAlbumGenreRequest.builder()
                    .albumId(albumId)
                    .genreId(genreId)
                    .build();
            when(albumGenreRepository.existsById(compositeId)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> albumGenreService.create(request))
                    .isInstanceOf(AlbumGenreAlreadyExistsException.class);
        }
    }

    @Nested
    class UpdateAlbumId {
        @Test
        void shouldReCreateLink_whenTargetAlbumIsAvailable() {
            // given
            var newAlbumId = UUID.randomUUID();
            when(albumGenreRepository.findById(compositeId)).thenReturn(Optional.of(albumGenre));
            when(albumGenreRepository.existsById(new AlbumGenreId(newAlbumId, genreId))).thenReturn(false);

            // when
            albumGenreService.updateAlbumId(albumId, genreId, newAlbumId);

            // then
            verify(albumGenreRepository).deleteById(compositeId);
            verify(albumGenreRepository).save(any(AlbumGenre.class));
        }

        @Test
        void shouldThrowException_whenCurrentLinkNotFound() {
            // given
            var newAlbumId = UUID.randomUUID();
            when(albumGenreRepository.findById(compositeId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumGenreService.updateAlbumId(albumId, genreId, newAlbumId))
                    .isInstanceOf(AlbumGenreNotFoundException.class);
        }

        @Test
        void shouldThrowException_whenGenreAlreadyLinkedToTargetAlbum() {
            // given
            var newAlbumId = UUID.randomUUID();
            when(albumGenreRepository.findById(compositeId)).thenReturn(Optional.of(albumGenre));
            when(albumGenreRepository.existsById(new AlbumGenreId(newAlbumId, genreId))).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> albumGenreService.updateAlbumId(albumId, genreId, newAlbumId))
                    .isInstanceOf(AlbumGenreAlreadyExistsException.class);
        }
    }

    @Nested
    class UpdateGenreId {
        @Test
        void shouldReCreateLink_whenTargetGenreIsAvailable() {
            // given
            var newGenreId = UUID.randomUUID();
            when(albumGenreRepository.findById(compositeId)).thenReturn(Optional.of(albumGenre));
            when(albumGenreRepository.existsById(new AlbumGenreId(albumId, newGenreId))).thenReturn(false);

            // when
            albumGenreService.updateGenreId(albumId, genreId, newGenreId);

            // then
            verify(albumGenreRepository).deleteById(compositeId);
            verify(albumGenreRepository).save(any(AlbumGenre.class));
        }

        @Test
        void shouldThrowException_whenCurrentLinkNotFound() {
            // given
            var newGenreId = UUID.randomUUID();
            when(albumGenreRepository.findById(compositeId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumGenreService.updateGenreId(albumId, genreId, newGenreId))
                    .isInstanceOf(AlbumGenreNotFoundException.class);
        }

        @Test
        void shouldThrowException_whenTargetGenreAlreadyLinkedToAlbum() {
            // given
            var newGenreId = UUID.randomUUID();
            when(albumGenreRepository.findById(compositeId)).thenReturn(Optional.of(albumGenre));
            when(albumGenreRepository.existsById(new AlbumGenreId(albumId, newGenreId))).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> albumGenreService.updateGenreId(albumId, genreId, newGenreId))
                    .isInstanceOf(AlbumGenreAlreadyExistsException.class);
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeleteLink_whenLinkExists() {
            // given
            when(albumGenreRepository.findById(compositeId)).thenReturn(Optional.of(albumGenre));

            // when
            albumGenreService.delete(albumId, genreId);

            // then
            verify(albumGenreRepository).deleteById(compositeId);
        }

        @Test
        void shouldThrowException_whenLinkNotFound() {
            // given
            when(albumGenreRepository.findById(compositeId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> albumGenreService.delete(albumId, genreId))
                    .isInstanceOf(AlbumGenreNotFoundException.class);
            verify(albumGenreRepository, never()).deleteById(any());
        }
    }
}
