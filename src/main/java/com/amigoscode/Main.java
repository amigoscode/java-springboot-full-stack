package com.amigoscode;

import com.amigoscode.product.Product;
import com.amigoscode.product.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            ProductRepository productRepository) {
        return args -> {
            Product product = new Product();
            product.setName("Macbook Pro");
            product.setDescription("Macbook Pro M4");
            product.setPrice(new BigDecimal(3000));
            product.setId(UUID.fromString(
                    "d95062e6-9f0b-4224-bc9d-d0723949848f")
            );
            product.setStockLevel(100);
            productRepository.save(product);
        };
    }

}
