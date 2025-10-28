package com.amigoscode.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTestSimple {

    private ProductRepository productRepository;
    private ProductImageService productImageService;
    private ProductService underTest;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productImageService = mock(ProductImageService.class);
        underTest = new ProductService(productRepository, productImageService);
    }

    @Test
    void canGetAllProducts() {
        // given
        when(productRepository.findAll()).thenReturn(List.of());

        // when
        List<ProductResponse> allProducts = underTest.getAllProducts();

        // then
        assertThat(allProducts).isEmpty();
        verify(productRepository).findAll();
    }
}
