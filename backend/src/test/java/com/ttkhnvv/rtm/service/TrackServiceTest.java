package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.track.CreateTrackRequest;
import com.ttkhnvv.rtm.dto.track.TrackFilter;
import com.ttkhnvv.rtm.dto.track.TrackResponse;
import com.ttkhnvv.rtm.entity.track.Track;
import com.ttkhnvv.rtm.exception.track.TrackNotFoundException;
import com.ttkhnvv.rtm.exception.track.TrackPositionAlreadyTakenException;
import com.ttkhnvv.rtm.mapper.TrackMapper;
import com.ttkhnvv.rtm.repository.track.TrackRepository;
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
class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;
    @Mock
    private TrackMapper trackMapper;

    @InjectMocks
    private TrackService trackService;

    private UUID trackId;
    private UUID albumId;
    private Track track;
    private TrackResponse trackResponse;

    @BeforeEach
    void init() {
        trackId = UUID.randomUUID();
        albumId = UUID.randomUUID();
        track = Track.builder()
                .id(trackId)
                .title("Track One")
                .albumId(albumId)
                .trackNumber(1)
                .durationSeconds(240)
                .build();
        trackResponse = TrackResponse.builder()
                .id(trackId)
                .title("Track One")
                .albumId(albumId)
                .trackNumber(1)
                .durationSeconds(240)
                .build();
    }

    @Nested
    class GetAll {
        @Test
        void shouldReturnPageOfTracks_whenFilterMatches() {
            // given
            var filter = new TrackFilter();
            filter.setAlbumId(albumId);
            var pageable = Pageable.unpaged();
            when(trackRepository.findAll((Specification<Track>) any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(track)));
            when(trackMapper.toResponse(track)).thenReturn(trackResponse);

            // when
            var result = trackService.getAll(filter, pageable);

            // then
            assertThat(result.getContent()).containsExactly(trackResponse);
        }

        @Test
        void shouldReturnEmptyPage_whenNoTracksMatch() {
            // given
            var filter = new TrackFilter();
            var pageable = Pageable.unpaged();
            when(trackRepository.findAll((Specification<Track>) any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            // when
            var result = trackService.getAll(filter, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    class GetById {
        @Test
        void shouldReturnTrackResponse_whenTrackExists() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
            when(trackMapper.toResponse(track)).thenReturn(trackResponse);

            // when
            var result = trackService.getById(trackId);

            // then
            assertThat(result).isEqualTo(trackResponse);
        }

        @Test
        void shouldThrowException_whenTrackNotFound() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> trackService.getById(trackId))
                    .isInstanceOf(TrackNotFoundException.class);
        }
    }

    @Nested
    class Create {
        @Test
        void shouldCreateAndReturnTrack_whenPositionIsAvailable() {
            // given
            var request = CreateTrackRequest.builder()
                    .title("Track One")
                    .albumId(albumId)
                    .trackNumber(1)
                    .durationSeconds(240)
                    .build();
            when(trackRepository.existsByAlbumIdAndTrackNumber(albumId, 1)).thenReturn(false);
            when(trackRepository.save(any(Track.class))).thenReturn(track);
            when(trackMapper.toResponse(track)).thenReturn(trackResponse);

            // when
            var result = trackService.create(request);

            // then
            assertThat(result).isEqualTo(trackResponse);
        }

        @Test
        void shouldThrowException_whenTrackNumberAlreadyTakenInAlbum() {
            // given
            var request = CreateTrackRequest.builder()
                    .title("Track One")
                    .albumId(albumId)
                    .trackNumber(1)
                    .durationSeconds(240)
                    .build();
            when(trackRepository.existsByAlbumIdAndTrackNumber(albumId, 1)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> trackService.create(request))
                    .isInstanceOf(TrackPositionAlreadyTakenException.class);
            verify(trackRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateTitle {
        @Test
        void shouldUpdateTitle_whenTrackExists() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

            // when
            trackService.updateTitle(trackId, "New Title");

            // then
            assertThat(track.getTitle()).isEqualTo("New Title");
            verify(trackRepository).save(track);
        }

        @Test
        void shouldThrowException_whenTrackNotFound() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> trackService.updateTitle(trackId, "New Title"))
                    .isInstanceOf(TrackNotFoundException.class);
        }
    }

    @Nested
    class UpdateAlbumId {
        @Test
        void shouldUpdateAlbum_whenTrackNumberIsAvailableInTargetAlbum() {
            // given
            var newAlbumId = UUID.randomUUID();
            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
            when(trackRepository.existsByAlbumIdAndTrackNumberAndIdNot(newAlbumId, 1, trackId)).thenReturn(false);

            // when
            trackService.updateAlbumId(trackId, newAlbumId);

            // then
            assertThat(track.getAlbumId()).isEqualTo(newAlbumId);
            verify(trackRepository).save(track);
        }

        @Test
        void shouldThrowException_whenTrackNotFound() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> trackService.updateAlbumId(trackId, UUID.randomUUID()))
                    .isInstanceOf(TrackNotFoundException.class);
        }

        @Test
        void shouldThrowException_whenTrackNumberAlreadyTakenInTargetAlbum() {
            // given
            var newAlbumId = UUID.randomUUID();
            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
            when(trackRepository.existsByAlbumIdAndTrackNumberAndIdNot(newAlbumId, 1, trackId)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> trackService.updateAlbumId(trackId, newAlbumId))
                    .isInstanceOf(TrackPositionAlreadyTakenException.class);
        }
    }

    @Nested
    class UpdateTrackNumber {
        @Test
        void shouldUpdateTrackNumber_whenPositionIsAvailable() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
            when(trackRepository.existsByAlbumIdAndTrackNumberAndIdNot(albumId, 2, trackId)).thenReturn(false);

            // when
            trackService.updateTrackNumber(trackId, 2);

            // then
            assertThat(track.getTrackNumber()).isEqualTo(2);
            verify(trackRepository).save(track);
        }

        @Test
        void shouldThrowException_whenTrackNotFound() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> trackService.updateTrackNumber(trackId, 2))
                    .isInstanceOf(TrackNotFoundException.class);
        }

        @Test
        void shouldThrowException_whenPositionAlreadyTakenInAlbum() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
            when(trackRepository.existsByAlbumIdAndTrackNumberAndIdNot(albumId, 2, trackId)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> trackService.updateTrackNumber(trackId, 2))
                    .isInstanceOf(TrackPositionAlreadyTakenException.class);
        }
    }

    @Nested
    class UpdateDurationSeconds {
        @Test
        void shouldUpdateDuration_whenTrackExists() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

            // when
            trackService.updateDurationSeconds(trackId, 300);

            // then
            assertThat(track.getDurationSeconds()).isEqualTo(300);
            verify(trackRepository).save(track);
        }

        @Test
        void shouldThrowException_whenTrackNotFound() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> trackService.updateDurationSeconds(trackId, 300))
                    .isInstanceOf(TrackNotFoundException.class);
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeleteTrack_whenTrackExists() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));

            // when
            trackService.delete(trackId);

            // then
            verify(trackRepository).deleteById(trackId);
        }

        @Test
        void shouldThrowException_whenTrackNotFound() {
            // given
            when(trackRepository.findById(trackId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> trackService.delete(trackId))
                    .isInstanceOf(TrackNotFoundException.class);
            verify(trackRepository, never()).deleteById(any());
        }
    }
}
