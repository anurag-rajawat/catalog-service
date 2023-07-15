package com.asr.catalogservice.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("find all products, should return all products")
    void findAllProducts() {
        // Given
        var product1 = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        var product2 = Product.of("Name2", "Description2", "Manufacturer2", 2.0, 2L);
        var products = List.of(product1, product2);

        // When
        when(productRepository.findAll())
                .thenReturn(products);

        // Then
        assertThat(productService.findAllProducts())
                .containsExactlyInAnyOrderElementsOf(products);
    }

    @Test
    @DisplayName("find product by id when not exists, should throw exception")
    void findProductById_whenNotExists_shouldThrowException() {
        // Given
        String productId = "64b13f81160f6f18fe1fdd49";

        // When
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> productService.findProductById(productId))
                .withMessage("Product with ID '" + productId + "' was not found.");
    }

    @Test
    @DisplayName("find product by id when exists, should return that product")
    void findProductById_whenExists_shouldReturnProduct() {
        // Given
        String productId = "64b13f81160f6f18fe1fdd49";
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // When + Then
        assertThat(productService.findProductById(productId))
                .isEqualTo(product);
    }

    @Test
    @DisplayName("save product when already exists, should throw exception")
    void saveProduct_whenAlreadyExists_shouldThrowException() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        when(productRepository.existsByName(product.name()))
                .thenReturn(true);

        // When + Then
        assertThatExceptionOfType(ProductAlreadyExistsException.class)
                .isThrownBy(() -> productService.saveProduct(product))
                .withMessage("Product with name '" + product.name() + "' already exists.");
    }

    @Test
    @DisplayName("save product when not exists, should save product")
    void saveProduct_whenNotExists_shouldSaveProduct() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        when(productRepository.save(product))
                .thenReturn(product);

        // When + Then
        assertThat(productService.saveProduct(product))
                .isEqualTo(product);
    }

    @NullSource
    @ValueSource(longs = {0L})
    @ParameterizedTest(name = "when units are ''{0}'', save product with default (1) unit")
    @DisplayName("save product with null and 0 units")
    void saveProduct_whenUnitsAreInvalid_shouldSaveProductWithDefaultUnit(Long units) {
        // Given
        var product = new Product(null, "Name", "Description", "Manufacturer", 1.0, units,
                null, null, 0);
        // Product is immutable, so we need a brand-new product will all expected changes
        var expectedProduct = new Product(null, "Name", "Description", "Manufacturer", 1.0, 1L,
                null, null, 0);
        when(productRepository.save(expectedProduct))
                .thenReturn(expectedProduct);

        // When + Then
        assertThat(productService.saveProduct(product).units())
                .isOne();
    }

    @Test
    @DisplayName("delete product by id when not exists, should throw exception")
    void deleteProductById_whenNotExists_shouldThrowException() {
        // Given
        String productId = "64b13f81160f6f18fe1fdd49";
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        when(productRepository.existsById(productId)).thenReturn(false);

        // When + Then
        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> productService.deleteProductById(productId))
                .withMessage("Product with ID '" + productId + "' was not found.");
    }

    @Test
    @DisplayName("update product, should update the product")
    void updateProduct_shouldUpdate() {
        // Given
        String productId = "64b13f81160f6f18fe1fdd49";
        var existingProduct = new Product(productId, "Existing Product", "Existing Description",
                "Existing Manufacturer", 1.0, 1L, Instant.now(), null, 0);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        var updatedProduct = new Product(existingProduct.id(), existingProduct.name(), "Updated Description", "Updated Manufacturer",
                2.0, 2L, existingProduct.createdDate(), existingProduct.lastModifiedDate(), existingProduct.version());

        when(productRepository.save(updatedProduct)).thenReturn(updatedProduct);

        // When
        Product actualProduct = productService.updateProduct(productId, updatedProduct);

        // Then
        assertThat(actualProduct)
                .isEqualTo(updatedProduct);
    }

}