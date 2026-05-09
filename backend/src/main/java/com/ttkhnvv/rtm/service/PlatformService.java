package com.ttkhnvv.rtm.service;

import com.ttkhnvv.rtm.dto.platform.CreatePlatformRequest;
import com.ttkhnvv.rtm.dto.platform.PlatformResponse;
import com.ttkhnvv.rtm.entity.platform.Platform;
import com.ttkhnvv.rtm.exception.platform.PlatformNotFoundException;
import com.ttkhnvv.rtm.mapper.PlatformMapper;
import com.ttkhnvv.rtm.repository.platform.PlatformRepository;
import com.ttkhnvv.rtm.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatformService {
    private final PlatformRepository platformRepository;
    private final PlatformMapper platformMapper;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public List<PlatformResponse> getAll() {
        return platformRepository.findAll().stream()
                .map(this::toResponseWithLogoUrl)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlatformResponse getById(UUID id) {
        return toResponseWithLogoUrl(findPlatformById(id));
    }

    @Transactional
    public PlatformResponse create(CreatePlatformRequest request) {
        var platform = Platform.builder()
                .name(request.getName())
                .build();
        return platformMapper.toResponse(platformRepository.save(platform));
    }

    @Transactional
    public void updateName(UUID id, String name) {
        var platform = findPlatformById(id);
        platform.setName(name);
        platformRepository.save(platform);
    }

    @Transactional
    public void updateLogo(UUID id, MultipartFile file) {
        var platform = findPlatformById(id);
        if (platform.getLogoKey() != null)
            storageService.delete(platform.getLogoKey());
        platform.setLogoKey(storageService.upload(file));
        platformRepository.save(platform);
    }

    @Transactional
    public void delete(UUID id) {
        var platform = findPlatformById(id);
        if (platform.getLogoKey() != null)
            storageService.delete(platform.getLogoKey());
        platformRepository.deleteById(id);
    }

    private PlatformResponse toResponseWithLogoUrl(Platform platform) {
        var response = platformMapper.toResponse(platform);
        if (platform.getLogoKey() != null)
            response.setLogoUrl(storageService.getPresignedUrl(platform.getLogoKey()));
        return response;
    }

    private Platform findPlatformById(UUID id) {
        return platformRepository.findById(id)
                .orElseThrow(() -> new PlatformNotFoundException("Failed to find platform."));
    }
}
