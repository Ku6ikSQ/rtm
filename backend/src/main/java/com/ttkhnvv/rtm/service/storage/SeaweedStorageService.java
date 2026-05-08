package com.ttkhnvv.rtm.service.storage;

import com.ttkhnvv.rtm.config.StorageProperties;
import com.ttkhnvv.rtm.exception.storage.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeaweedStorageService implements StorageService {
    private final StorageProperties properties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public String upload(MultipartFile file) {
        if (file.isEmpty())
            throw new StorageException("File is empty");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new StorageException("Invalid file type");
        var key = getKey(file.getOriginalFilename());
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(properties.getBucket())
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new StorageException("Failed to upload file", e);
        }
        return key;
    }

    public void delete(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(key)
                        .build()
        );
    }

    @Override
    public String getPresignedUrl(String key) {
        var request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(r -> r
                        .bucket(properties.getBucket())
                        .key(key))
                .build();

        return s3Presigner.presignGetObject(request).url().toString();
    }

    private String getKey(String filename) {
        filename = (filename == null ? "unknown" : filename);
        return UUID.randomUUID() + "_" + filename;
    }

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
}
