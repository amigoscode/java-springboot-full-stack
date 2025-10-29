package com.amigoscode.product;

import com.amigoscode.exception.ResourceNotFound;
import com.amigoscode.storage.S3StorageService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProductImageService {

    private final ProductRepository productRepository;
    private final S3StorageService s3;

    public ProductImageService(ProductRepository productRepository, S3StorageService s3) {
        this.productRepository = productRepository;
        this.s3 = s3;
    }

    public void uploadProductImage(UUID productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound("product with id [" + productId + "] not found"));

        String filename = Objects.requireNonNullElse(file.getOriginalFilename(), "image");
        String contentType = Objects.requireNonNullElse(file.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE);
        String key = s3.computeProductImageKey(productId, filename);
        try {
            s3.upload(file.getBytes(), contentType, key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
        product.setImageUrl(key);
        productRepository.save(product);
    }

    public S3StorageService.StoredObject downloadProductImage(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound("product with id [" + productId + "] not found"));
        String key = product.getImageUrl();
        if (key == null || key.isBlank()) {
            throw new ResourceNotFound("product with id [" + productId + "] does not have an image");
        }
        return s3.download(key)
                .orElseThrow(() -> new ResourceNotFound("image for product with id [" + productId + "] not found"));
    }
}
