package com.example.chat.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileService {

    @Value("${app.minio.endpoint}")
    private String minioEndpoint;

    @Value("${app.minio.access-key}")
    private String accessKey;

    @Value("${app.minio.secret-key}")
    private String secretKey;

    @Value("${app.minio.bucket-name}")
    private String bucketName;

    @Value("${app.file.upload.max-size}")
    private String maxSize;

    @Value("${app.file.upload.allowed-types}")
    private String allowedTypes;

    private MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public String uploadFile(MultipartFile file, Long userId) throws Exception {
        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String objectName = "uploads/" + userId + "/" + fileName;

        MinioClient minioClient = getMinioClient();

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
        }

        return getFileUrl(objectName);
    }

    public String getFileUrl(String objectName) throws Exception {
        MinioClient minioClient = getMinioClient();
        
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(7, TimeUnit.DAYS)
                .build()
        );
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Check file size (simplified - you'd parse maxSize properly)
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new RuntimeException("File size exceeds limit");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new RuntimeException("File type not allowed");
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}