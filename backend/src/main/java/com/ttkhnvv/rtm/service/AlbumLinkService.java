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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages streaming platform links for albums.
 * Platform names and logo URLs are embedded in the response to avoid N+1 queries.
 */
@Service
@RequiredArgsConstructor
public class AlbumLinkService {
    private final AlbumLinkRepository albumLinkRepository;
    private final PlatformRepository platformRepository;
    private final StorageService storageService;

    /**
     * Returns all streaming platform links for the given album.
     * Platform name and logo URL are embedded in each response — no N+1.
     *
     * @param albumId album identifier
     * @return list of album link responses with embedded platform info
     */
    @Transactional(readOnly = true)
    public List<AlbumLinkResponse> getByAlbumId(UUID albumId) {
        var links = albumLinkRepository.findAllByAlbumId(albumId);
        var platformIds = links.stream().map(AlbumLink::getPlatformId).distinct().toList();
        var platformMap = platformRepository.findAllById(platformIds).stream()
                .collect(Collectors.toMap(Platform::getId, p -> p));
        return links.stream()
                .map(link -> toResponse(link, platformMap))
                .toList();
    }

    /**
     * Creates a new streaming platform link for an album.
     * Each album may have at most one link per platform.
     *
     * @param request link data (albumId, platformId, url)
     * @return the created album link response with embedded platform info
     * @throws AlbumLinkAlreadyExistsException if the album already has a link for the given platform
     */
    @Transactional
    public AlbumLinkResponse create(CreateAlbumLinkRequest request) {
        var id = new AlbumLinkId(request.getAlbumId(), request.getPlatformId());
        if (albumLinkRepository.existsById(id))
            throw new AlbumLinkAlreadyExistsException("This album already has a link for the given platform.");
        var link = AlbumLink.builder()
                .albumId(request.getAlbumId())
                .platformId(request.getPlatformId())
                .url(request.getUrl())
                .build();
        var saved = albumLinkRepository.save(link);
        var platformMap = platformRepository.findAllById(List.of(request.getPlatformId())).stream()
                .collect(Collectors.toMap(Platform::getId, p -> p));
        return toResponse(saved, platformMap);
    }

    /**
     * Removes a streaming platform link from an album.
     *
     * @param albumId    album identifier
     * @param platformId platform identifier
     * @throws AlbumLinkNotFoundException if no link exists for the given album-platform pair
     */
    @Transactional
    public void delete(UUID albumId, UUID platformId) {
        var id = new AlbumLinkId(albumId, platformId);
        if (!albumLinkRepository.existsById(id))
            throw new AlbumLinkNotFoundException("Failed to find album link.");
        albumLinkRepository.deleteById(id);
    }

    private AlbumLinkResponse toResponse(AlbumLink link, Map<UUID, Platform> platformMap) {
        var platform = platformMap.get(link.getPlatformId());
        return AlbumLinkResponse.builder()
                .albumId(link.getAlbumId())
                .platformId(link.getPlatformId())
                .url(link.getUrl())
                .platformName(platform != null ? platform.getName() : null)
                .platformLogoUrl(platform != null && platform.getLogoKey() != null
                        ? storageService.getPresignedUrl(platform.getLogoKey()) : null)
                .build();
    }
}