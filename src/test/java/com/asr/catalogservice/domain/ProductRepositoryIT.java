package com.asr.catalogservice.domain;

import com.asr.catalogservice.config.DataConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
@Import(DataConfig.class)
@ActiveProfiles("integration")
class ProductRepositoryIT {
    @Container
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("find product by id when exists, should return product")
    void findProductById_whenExists() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);

        // When
        var productId = mongoTemplate.save(product).id();
        Optional<Product> actualProduct = productRepository.findById(productId);

        // Then
        assertThat(actualProduct).isPresent()
                .get()
                .satisfies(p -> {
                    assertThat(p.name()).isEqualTo(product.name());
                    assertThat(p.description()).isEqualTo(product.description());
                    assertThat(p.manufacturer()).isEqualTo(product.manufacturer());
                    assertThat(p.price()).isEqualTo(product.price());
                    assertThat(p.units()).isEqualTo(product.units());
                });
    }

    @Test
    @DisplayName("find product by id when not exists, should return empty product")
    void findProductById_whenNotExists() {
        // Given
        var productId = "64b13314f6f13567f4c9c705";

        // When
        Optional<Product> actualProduct = productRepository.findById(productId);

        // Then
        assertThat(actualProduct).isEmpty();
    }

    @Test
    @DisplayName("find all existing products")
    void findAllProducts() {
        // Given
        var product1 = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        var product2 = Product.of("Name2", "Description2", "Manufacturer2", 2.0, 2L);

        // When
        var product1Id = mongoTemplate.save(product1).id();
        var product2Id = mongoTemplate.save(product2).id();
        Iterable<Product> products = productRepository.findAll();

        // Then
        assertThat(StreamSupport.stream(products.spliterator(), true)
                .filter(product -> product.id().equals(product1Id) || product.id().equals(product2Id))
                .collect(Collectors.toList())).hasSize(2);
    }

    @Test
    @DisplayName("exist by id when exists, should return true")
    void existsById_whenExists() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);

        // When
        var productId = mongoTemplate.save(product).id();
        boolean exists = productRepository.existsById(productId);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("exist by id when not exists, should return false")
    void existsById_whenNotExists() {
        // Given
        var productId = "64b13314f6f13567f4c9c705";

        // When
        boolean exists = productRepository.existsById(productId);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("exist by name when exists, should return true")
    void existByName_whenExists() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);

        // When
        mongoTemplate.save(product);
        boolean exists = productRepository.existsByName(product.name());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("exist by name when exists, should return false")
    void existByName_whenNotExists() {
        // Given
        var productName = "name";

        // When
        boolean exists = productRepository.existsByName(productName);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("delete by id when exists, should delete product")
    void deleteProductById_whenExists() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        var productId = mongoTemplate.save(product).id();

        // When
        productRepository.deleteById(productId);
        Product actualProduct = mongoTemplate.findById(productId, Product.class);

        // Then
        assertThat(actualProduct).isNull();
    }
}