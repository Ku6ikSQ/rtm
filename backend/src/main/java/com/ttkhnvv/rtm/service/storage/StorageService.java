package com.ttkhnvv.rtm.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over object storage for uploading, deleting and generating presigned read URLs.
 */
public interface StorageService {

    /**
     * Uploads a file to object storage and returns its storage key.
     *
     * @param file multipart file to upload
     * @return the storage key assigned to the uploaded file
     */
    String upload(MultipartFile file);

    /**
     * Permanently deletes an object from storage by its key.
     *
     * @param key storage key of the object to delete
     */
    void delete(String key);

    /**
     * Generates a presigned URL that grants temporary read access to the object.
     *
     * @param key storage key of the object
     * @return presigned URL valid for a limited time
     */
    String getPresignedUrl(String key);
}
