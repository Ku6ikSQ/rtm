package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.genre.CreateGenreRequest;
import com.ttkhnvv.rtm.dto.genre.GenreFilter;
import com.ttkhnvv.rtm.dto.genre.GenreResponse;
import com.ttkhnvv.rtm.entity.genre.Genre;
import com.ttkhnvv.rtm.exception.genre.GenreNotFoundException;
import com.ttkhnvv.rtm.exception.genre.GenreSlugAlreadyTakenException;
import com.ttkhnvv.rtm.mapper.GenreMapper;
import com.ttkhnvv.rtm.repository.albumgenre.AlbumGenreRepository;
import com.ttkhnvv.rtm.repository.genre.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;
    @Mock
    private AlbumGenreRepository albumGenreRepository;
    @Mock
    private GenreMapper genreMapper;

    @InjectMocks
    private GenreService genreService;

    private UUID genreId;
    private Genre genre;
    private GenreResponse genreResponse;

    @BeforeEach
    void init() {
        genreId = UUID.randomUUID();
        genre = Genre.builder()
                .id(genreId)
                .name("Rock")
                .slug("rock")
                .description("Rock music")
                .build();
        genreResponse = GenreResponse.builder()
                .id(genreId)
                .name("Rock")
                .slug("rock")
                .description("Rock music")
                .build();
    }

    @Nested
    class GetAll {
        @Test
        void shouldReturnPageOfGenres_whenFilterMatches() {
            // given
            var filter = new GenreFilter();
            filter.setName("Rock");
            var pageable = Pageable.unpaged();
            when(genreRepository.findAll((Specification<Genre>) any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(genre)));
            when(genreMapper.toResponse(genre)).thenReturn(genreResponse);
            when(albumGenreRepository.countGroupedByGenreIdIn(any())).thenReturn(List.of());

            // when
            var result = genreService.getAll(filter, pageable);

            // then
            assertThat(result.getContent()).containsExactly(genreResponse);
        }

        @Test
        void shouldReturnEmptyPage_whenNoGenresMatch() {
            // given
            var filter = new GenreFilter();
            var pageable = Pageable.unpaged();
            when(genreRepository.findAll((Specification<Genre>) any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            // when
            var result = genreService.getAll(filter, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    class GetById {
        @Test
        void shouldReturnGenreResponse_whenGenreExists() {
            // given
            when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
            when(genreMapper.toResponse(genre)).thenReturn(genreResponse);
            when(albumGenreRepository.countByGenreId(genreId)).thenReturn(5L);

            // when
            var result = genreService.getById(genreId);

            // then
            assertThat(result.getAlbumCount()).isEqualTo(5L);
        }

        @Test
        void shouldThrowException_whenGenreNotFound() {
            // given
            when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> genreService.getById(genreId))
                    .isInstanceOf(GenreNotFoundException.class);
        }
    }

    @Nested
    class Create {
        @Test
        void shouldCreateAndReturnGenre_whenSlugIsUnique() {
            // given
            var request = CreateGenreRequest.builder()
                    .name("Rock")
                    .slug("rock")
                    .description("Rock music")
                    .build();
            when(genreRepository.existsBySlug("rock")).thenReturn(false);
            when(genreRepository.save(any(Genre.class))).thenReturn(genre);
            when(genreMapper.toResponse(genre)).thenReturn(genreResponse);

            // when
            var result = genreService.create(request);

            // then
            assertThat(result).isEqualTo(genreResponse);
        }

        @Test
        void shouldThrowException_whenSlugAlreadyTaken() {
            // given
            var request = CreateGenreRequest.builder()
                    .name("Rock")
                    .slug("rock")
                    .build();
            when(genreRepository.existsBySlug("rock")).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> genreService.create(request))
                    .isInstanceOf(GenreSlugAlreadyTakenException.class);
        }
    }

    @Nested
    class UpdateName {
        @Test
        void shouldUpdateName_whenGenreExists() {
            // given
            when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

            // when
            genreService.updateName(genreId, "Metal");

            // then
            assertThat(genre.getName()).isEqualTo("Metal");
            verify(genreRepository).save(genre);
        }

        @Test
        void shouldThrowException_whenGenreNotFound() {
            // given
            when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> genreService.updateName(genreId, "Metal"))
                    .isInstanceOf(GenreNotFoundException.class);
        }
    }

    @Nested
    class UpdateSlug {
        @Test
        void shouldUpdateSlug_whenNewSlugIsUnique() {
            // given
            when(genreRepository.existsBySlugAndIdNot("metal", genreId)).thenReturn(false);
            when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

            // when
            genreService.updateSlug(genreId, "metal");

            // then
            assertThat(genre.getSlug()).isEqualTo("metal");
            verify(genreRepository).save(genre);
        }

        @Test
        void shouldThrowException_whenSlugTakenByAnotherGenre() {
            // given
            when(genreRepository.existsBySlugAndIdNot("metal", genreId)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> genreService.updateSlug(genreId, "metal"))
                    .isInstanceOf(GenreSlugAlreadyTakenException.class);
            verify(genreRepository, never()).findById(any());
        }

        @Test
        void shouldThrowException_whenGenreNotFound() {
            // given
            when(genreRepository.existsBySlugAndIdNot("metal", genreId)).thenReturn(false);
            when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> genreService.updateSlug(genreId, "metal"))
                    .isInstanceOf(GenreNotFoundException.class);
        }
    }

    @Nested
    class UpdateDescription {
        @Test
        void shouldUpdateDescription_whenGenreExists() {
            // given
            when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

            // when
            genreService.updateDescription(genreId, "Updated description");

            // then
            assertThat(genre.getDescription()).isEqualTo("Updated description");
            verify(genreRepository).save(genre);
        }

        @Test
        void shouldThrowException_whenGenreNotFound() {
            // given
            when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> genreService.updateDescription(genreId, "Updated description"))
                    .isInstanceOf(GenreNotFoundException.class);
        }
    }

    @Nested
    class UpdateParent {
        @Test
        void shouldUpdateParent_whenGenreExists() {
            // given
            var parentId = UUID.randomUUID();
            when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

            // when
            genreService.updateParent(genreId, parentId);

            // then
            assertThat(genre.getParentId()).isEqualTo(parentId);
            verify(genreRepository).save(genre);
        }

        @Test
        void shouldThrowException_whenGenreNotFound() {
            // given
            when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> genreService.updateParent(genreId, UUID.randomUUID()))
                    .isInstanceOf(GenreNotFoundException.class);
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeleteGenre_whenGenreExists() {
            // given
            when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

            // when
            genreService.delete(genreId);

            // then
            verify(genreRepository).deleteById(genreId);
        }

        @Test
        void shouldThrowException_whenGenreNotFound() {
            // given
            when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> genreService.delete(genreId))
                    .isInstanceOf(GenreNotFoundException.class);
            verify(genreRepository, never()).deleteById(any());
        }
    }
}
