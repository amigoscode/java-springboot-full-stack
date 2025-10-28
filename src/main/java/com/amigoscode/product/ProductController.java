package com.amigoscode.product;

import com.amigoscode.storage.S3StorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;

    public ProductController(ProductService productService, ProductImageService productImageService) {
        this.productService = productService;
        this.productImageService = productImageService;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("{id}")
    public ProductResponse getProductById(@PathVariable("id") UUID id) {
        return productService.getProductById(id);
    }

    @DeleteMapping("{id}")
    public void deleteProductById(@PathVariable("id") UUID id) {
        productService.deleteProductById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID saveProduct(@RequestBody @Valid NewProductRequest product) {
        return productService.saveNewProduct(product);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UUID saveProductWithImage(@RequestParam("name") @NotBlank String name,
                                     @RequestParam("description") @NotBlank String description,
                                     @RequestParam("price") @NotBlank String price,
                                     @RequestParam("stockLevel") @NotBlank String stockLevel,
                                     @RequestParam(value = "image", required = false) MultipartFile image) {
        return productService.saveNewProductWithImage(name, description, price, stockLevel, image);
    }

    @PutMapping("{id}")
    public void updateProduct(@PathVariable UUID id,
                              @RequestBody @Valid UpdateProductRequest request) {
        productService.updateProduct(id, request);
    }

    @PostMapping("{id}/image")
    @ResponseStatus(HttpStatus.OK)
    public void uploadProductImage(@PathVariable UUID id,
                                   @RequestParam("file") MultipartFile file) {
        productImageService.uploadProductImage(id, file);
    }

    @GetMapping("{id}/image")
    public ResponseEntity<byte[]> downloadProductImage(@PathVariable UUID id) {
        S3StorageService.StoredObject storedObject = productImageService.downloadProductImage(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(storedObject.contentType()));
        headers.setContentLength(storedObject.bytes().length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"product-image\"");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(storedObject.bytes());
    }
}
