package com.amigoscode.product;

import com.amigoscode.exception.ResourceNotFound;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageService productImageService;

    public ProductService(ProductRepository productRepository,
                          ProductImageService productImageService) {
        this.productRepository = productRepository;
        this.productImageService = productImageService;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(mapToResponse())
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(UUID id) {
        return productRepository.findById(id)
                .map(mapToResponse())
                .orElseThrow(() -> new ResourceNotFound(
                        "product with id [" + id + "] not found"
                ));
    }

    public void deleteProductById(UUID id) {
        boolean exists = productRepository.existsById(id);
        if (!exists) {
            throw new ResourceNotFound(
                    "product with id [" + id + "] not found"
            );
        }
        productRepository.deleteById(id);
    }

    public UUID saveNewProduct(NewProductRequest product) {
        UUID id = UUID.randomUUID();
        Product newProduct = new Product(
                id,
                product.name(),
                product.description(),
                product.price(),
                product.imageUrl(),
                product.stockLevel()
        );
        productRepository.save(newProduct);
        return id;
    }

    public UUID saveNewProductWithImage(String name, String description, String price, String stockLevel, MultipartFile image) {
        UUID id = UUID.randomUUID();
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Product description cannot be empty");
        }
        
        BigDecimal priceValue;
        try {
            priceValue = new BigDecimal(price);
            if (priceValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Price must be greater than 0");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price format: " + price);
        }
        
        Integer stockLevelValue;
        try {
            stockLevelValue = Integer.parseInt(stockLevel);
            if (stockLevelValue < 0) {
                throw new IllegalArgumentException("Stock level cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid stock level format: " + stockLevel);
        }
        
        // Create the product first
        Product newProduct = new Product(
                id,
                name.trim(),
                description.trim(),
                priceValue,
                null, // imageUrl will be set after upload
                stockLevelValue
        );
        productRepository.save(newProduct);
        
        // Upload image if provided
        if (image != null && !image.isEmpty()) {
            try {
                productImageService.uploadProductImage(id, image);
            } catch (Exception e) {
                // Log the error but don't fail the product creation
                System.err.println("Failed to upload image for product " + id + ": " + e.getMessage());
                // Optionally, you could delete the product if image upload is critical
                // productRepository.deleteById(id);
                //  throw new IllegalStateException("Product created but image upload failed", e);
            }
        }
        
        return id;
    }

    Function<Product, ProductResponse> mapToResponse() {
        return p -> new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getImageUrl(),
                p.getStockLevel(),
                p.getPublished(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getDeletedAt()
        );
    }


    public void updateProduct(UUID id,
                              UpdateProductRequest updateRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound(
                        "product with id [" + id + "] not found"
                ));

        if (updateRequest.name() != null && !updateRequest.name().equals(product.getName())) {
            product.setName(updateRequest.name());
        }
        if (updateRequest.description() != null && !updateRequest.description().equals(product.getDescription())) {
            product.setDescription(updateRequest.description());
        }
        if (updateRequest.price() != null && !updateRequest.price().equals(product.getPrice())) {
            product.setPrice(updateRequest.price());
        }
        if (updateRequest.imageUrl() != null && !updateRequest.imageUrl().equals(product.getImageUrl())) {
            product.setImageUrl(updateRequest.imageUrl());
        }
        if (updateRequest.stockLevel() != null && !updateRequest.stockLevel().equals(product.getStockLevel())) {
            product.setStockLevel(updateRequest.stockLevel());
        }

        if (updateRequest.isPublished() != null && !updateRequest.isPublished().equals(product.getPublished())) {
            product.setPublished(updateRequest.isPublished());
        }

        productRepository.save(product);
    }
}
