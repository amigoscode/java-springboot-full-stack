package com.amigoscode.product;

import com.amigoscode.exception.ResourceNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageService productImageService;
    private ProductService underTest;

    @BeforeEach
    void setUp() {
        underTest = new ProductService(productRepository, productImageService);
    }

    @Test
    void canGetAllProducts() {
        // given
        UUID productId = UUID.randomUUID();
        Product product = new Product(
                productId,
                "Test Product",
                "A test product description",
                BigDecimal.TEN,
                "https://example.com/image.png",
                10
        );
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        product.setPublished(true);

        when(productRepository.findAll()).thenReturn(List.of(product));

        // when
        List<ProductResponse> allProducts = underTest.getAllProducts();

        // then
        assertThat(allProducts).hasSize(1);
        ProductResponse response = allProducts.get(0);
        assertThat(response.id()).isEqualTo(productId);
        assertThat(response.name()).isEqualTo("Test Product");
        assertThat(response.description()).isEqualTo("A test product description");
        assertThat(response.price()).isEqualTo(BigDecimal.TEN);
        assertThat(response.imageUrl()).isEqualTo("https://example.com/image.png");
        assertThat(response.stockLevel()).isEqualTo(10);
        assertThat(response.isPublished()).isTrue();

        verify(productRepository).findAll();
    }

    @Test
    void canGetProductById() {
        // given
        UUID productId = UUID.randomUUID();
        Product product = new Product(
                productId,
                "Test Product",
                "A test product description",
                BigDecimal.TEN,
                "https://example.com/image.png",
                10
        );
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        product.setPublished(true);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // when
        ProductResponse response = underTest.getProductById(productId);

        // then
        assertThat(response.id()).isEqualTo(productId);
        assertThat(response.name()).isEqualTo("Test Product");
        assertThat(response.description()).isEqualTo("A test product description");
        assertThat(response.price()).isEqualTo(BigDecimal.TEN);
        assertThat(response.imageUrl()).isEqualTo("https://example.com/image.png");
        assertThat(response.stockLevel()).isEqualTo(10);
        assertThat(response.isPublished()).isTrue();

        verify(productRepository).findById(productId);
    }

    @Test
    void getProductByIdThrowsWhenProductNotFound() {
        // given
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> underTest.getProductById(productId))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("product with id [" + productId + "] not found");

        verify(productRepository).findById(productId);
    }

    @Test
    void canDeleteProductById() {
        // given
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(true);

        // when
        underTest.deleteProductById(productId);

        // then
        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    void deleteProductByIdThrowsWhenProductNotFound() {
        // given
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> underTest.deleteProductById(productId))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("product with id [" + productId + "] not found");

        verify(productRepository).existsById(productId);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void canSaveNewProduct() {
        // given
        NewProductRequest request = new NewProductRequest(
                "New Product",
                "A new product description",
                BigDecimal.valueOf(25.99),
                50,
                "https://example.com/image.png"
        );

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setCreatedAt(Instant.now());
            product.setUpdatedAt(Instant.now());
            return product;
        });

        // when
        UUID productId = underTest.saveNewProduct(request);

        // then
        assertThat(productId).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void canSaveNewProductWithImage() {
        // given
        String name = "Product with Image";
        String description = "A product with image description";
        String price = "29.99";
        String stockLevel = "25";
        MultipartFile mockImage = mock(MultipartFile.class);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setCreatedAt(Instant.now());
            product.setUpdatedAt(Instant.now());
            return product;
        });

        // when
        UUID productId = underTest.saveNewProductWithImage(name, description, price, stockLevel, mockImage);

        // then
        assertThat(productId).isNotNull();
        verify(productRepository).save(any(Product.class));
        verify(productImageService).uploadProductImage(productId, mockImage);
    }

    @Test
    void canSaveNewProductWithoutImage() {
        // given
        String name = "Product without Image";
        String description = "A product without image description";
        String price = "19.99";
        String stockLevel = "15";

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setCreatedAt(Instant.now());
            product.setUpdatedAt(Instant.now());
            return product;
        });

        // when
        UUID productId = underTest.saveNewProductWithImage(name, description, price, stockLevel, null);

        // then
        assertThat(productId).isNotNull();
        verify(productRepository).save(any(Product.class));
        verify(productImageService, never()).uploadProductImage(any(), any());
    }

    @Test
    void saveNewProductWithImageHandlesImageUploadFailure() {
        // given
        String name = "Product with Failed Image";
        String description = "A product with failed image upload";
        String price = "39.99";
        String stockLevel = "30";
        MultipartFile mockImage = mock(MultipartFile.class);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setCreatedAt(Instant.now());
            product.setUpdatedAt(Instant.now());
            return product;
        });

        doThrow(new RuntimeException("Image upload failed"))
                .when(productImageService).uploadProductImage(any(), any());

        // when
        UUID productId = underTest.saveNewProductWithImage(name, description, price, stockLevel, mockImage);

        // then
        assertThat(productId).isNotNull();
        verify(productRepository).save(any(Product.class));
        verify(productImageService).uploadProductImage(productId, mockImage);
    }

    @Test
    void saveNewProductWithImageValidatesInput() {
        // Test empty name
        assertThatThrownBy(() -> underTest.saveNewProductWithImage("", "description", "10.00", "5", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product name cannot be empty");

        // Test empty description
        assertThatThrownBy(() -> underTest.saveNewProductWithImage("name", "", "10.00", "5", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product description cannot be empty");

        // Test invalid price
        assertThatThrownBy(() -> underTest.saveNewProductWithImage("name", "description", "invalid", "5", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid price format");

        // Test negative price
        assertThatThrownBy(() -> underTest.saveNewProductWithImage("name", "description", "0", "5", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price must be greater than 0");

        // Test invalid stock level
        assertThatThrownBy(() -> underTest.saveNewProductWithImage("name", "description", "10.00", "invalid", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid stock level format");

        // Test negative stock level
        assertThatThrownBy(() -> underTest.saveNewProductWithImage("name", "description", "10.00", "-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock level cannot be negative");
    }

    @Test
    void canUpdateProduct() {
        // given
        UUID productId = UUID.randomUUID();
        Product existingProduct = new Product(
                productId,
                "Original Name",
                "Original Description",
                BigDecimal.valueOf(10.00),
                "original-image.png",
                5
        );
        existingProduct.setCreatedAt(Instant.now());
        existingProduct.setUpdatedAt(Instant.now());
        existingProduct.setPublished(true);

        UpdateProductRequest updateRequest = new UpdateProductRequest(
                "Updated Name",
                "Updated Description",
                "updated-image.png",
                BigDecimal.valueOf(15.00),
                10,
                false
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setUpdatedAt(Instant.now());
            return product;
        });

        // when
        underTest.updateProduct(productId, updateRequest);

        // then
        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);

        assertThat(existingProduct.getName()).isEqualTo("Updated Name");
        assertThat(existingProduct.getDescription()).isEqualTo("Updated Description");
        assertThat(existingProduct.getImageUrl()).isEqualTo("updated-image.png");
        assertThat(existingProduct.getPrice()).isEqualTo(BigDecimal.valueOf(15.00));
        assertThat(existingProduct.getStockLevel()).isEqualTo(10);
        assertThat(existingProduct.getPublished()).isFalse();
    }

    @Test
    void updateProductThrowsWhenProductNotFound() {
        // given
        UUID productId = UUID.randomUUID();
        UpdateProductRequest updateRequest = new UpdateProductRequest(
                "Updated Name",
                "Updated Description",
                "updated-image.png",
                BigDecimal.valueOf(15.00),
                10,
                false
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> underTest.updateProduct(productId, updateRequest))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("product with id [" + productId + "] not found");

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }
}