package com.amigoscode.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class S3StorageService {
    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket:}")
    private String bucket;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String computeProductImageKey(UUID productId, String filename) {
        String safe = filename == null ? "image" : filename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        return "products/" + productId + "/" + Instant.now().toEpochMilli() + "-" + safe;
    }

    public String upload(byte[] bytes, String contentType, String key) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(put, RequestBody.fromBytes(bytes));
        return key;
    }

    public Optional<StoredObject> download(String key) {
        try {
            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            try (InputStream is = s3Client.getObject(req); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = is.read(buf)) != -1) {
                    baos.write(buf, 0, r);
                }
                HeadObjectResponse meta = s3Client.headObject(b -> b.bucket(bucket).key(key));
                String contentType = meta.contentType();
                return Optional.of(new StoredObject(baos.toByteArray(), contentType));
            }
        } catch (NoSuchKeyException e) {
            log.warn("S3 key not found: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download S3 object: " + key, e);
        }
    }

    public record StoredObject(byte[] bytes, String contentType) {}
}
