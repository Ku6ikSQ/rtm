package com.ttkhnvv.rtm.service.storage;

import com.ttkhnvv.rtm.config.StorageProperties;
import com.ttkhnvv.rtm.exception.storage.StorageException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeaweedStorageServiceTest {

    @Mock
    private StorageProperties properties;
    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private SeaweedStorageService seaweedStorageService;

    @Nested
    class Upload {
        @Test
        void shouldUploadFileAndReturnKey_whenFileIsValid() {
            // given
            var file = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
            when(properties.getBucket()).thenReturn("test-bucket");
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // when
            var key = seaweedStorageService.upload(file);

            // then
            assertThat(key).endsWith("_photo.jpg");
            verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        void shouldThrowException_whenFileIsEmpty() {
            // given
            var file = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", new byte[0]);

            // when/then
            assertThatThrownBy(() -> seaweedStorageService.upload(file))
                    .isInstanceOf(StorageException.class);
        }

        @Test
        void shouldThrowException_whenFileTypeIsNotAllowed() {
            // given
            var file = new MockMultipartFile("doc", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

            // when/then
            assertThatThrownBy(() -> seaweedStorageService.upload(file))
                    .isInstanceOf(StorageException.class);
        }

        @Test
        void shouldAcceptPngFile_whenFileTypeIsPng() {
            // given
            var file = new MockMultipartFile("image", "image.png", "image/png", new byte[]{1, 2, 3});
            when(properties.getBucket()).thenReturn("test-bucket");
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // when
            var key = seaweedStorageService.upload(file);

            // then
            assertThat(key).endsWith("_image.png");
        }

        @Test
        void shouldGenerateKeyWithUnknownSuffix_whenOriginalFilenameIsNull() throws IOException {
            // given — use a Mockito mock so getOriginalFilename() can return null
            var file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getOriginalFilename()).thenReturn(null);
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
            when(file.getSize()).thenReturn(1L);
            when(properties.getBucket()).thenReturn("test-bucket");
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // when
            var key = seaweedStorageService.upload(file);

            // then
            assertThat(key).endsWith("_unknown");
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldCallS3DeleteObject_whenKeyIsProvided() {
            // given
            when(properties.getBucket()).thenReturn("test-bucket");
            when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .thenReturn(DeleteObjectResponse.builder().build());

            // when
            seaweedStorageService.delete("some-key");

            // then
            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        }
    }

    @Nested
    class GetPresignedUrl {
        @Test
        void shouldReturnPresignedUrl_whenKeyIsProvided() throws MalformedURLException {
            // given
            when(properties.getBucket()).thenReturn("test-bucket");
            var expectedUrl = URI.create("http://localhost/some-key").toURL();
            var presignedRequest = mock(PresignedGetObjectRequest.class);
            when(presignedRequest.url()).thenReturn(expectedUrl);
            when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

            // when
            var result = seaweedStorageService.getPresignedUrl("some-key");

            // then
            assertThat(result).isEqualTo(expectedUrl.toString());
        }
    }
}
