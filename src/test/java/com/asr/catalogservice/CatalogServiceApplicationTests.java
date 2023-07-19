package com.asr.catalogservice;

import com.asr.catalogservice.domain.Product;
import com.asr.catalogservice.domain.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogServiceApplicationTests {

    private static final String PRODUCT_ROOT_ENDPOINT = "/products";

    @Container
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @Autowired
    private WebTestClient testClient;

    @Autowired
    private ProductRepository productRepository;

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("when get request, then return all products")
    void whenGetRequest_thenProductsReturned() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        var product2 = Product.of("Name 2", "Description 2", "Manufacturer 2", 2.0, 2L);
        productRepository.saveAll(List.of(product, product2));

        // When + Then
        testClient.
                get()
                .uri(PRODUCT_ROOT_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class).value(actualProducts ->
                        assertThat(actualProducts).isNotEmpty()
                                .hasSize(2)
                );
    }

    @Test
    @DisplayName("when get request by product id, then product should be returned")
    void whenGetRequest_productExists_thenProductReturned() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        var productId = productRepository.save(product).id();

        // When + Then
        testClient
                .get()
                .uri(PRODUCT_ROOT_ENDPOINT + "/" + productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class).value(actualProduct -> {
                    assertProduct(actualProduct, product);
                });
    }

    @Test
    @DisplayName("when post request, then product should be created")
    void whenPostRequest_thenProductCreated() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);

        // When + Then
        testClient
                .post()
                .uri(PRODUCT_ROOT_ENDPOINT)
                .bodyValue(product)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Product.class).value(actualProduct ->
                        assertProduct(actualProduct, product)
                );
    }

    @Test
    @DisplayName("when put request, then product should be updated")
    void whenPutRequest_thenProductUpdated() {
        // Given
        var existingProduct = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        var productId = productRepository.save(existingProduct).id();
        var updatedProduct = new Product(productId, existingProduct.name(), "Updated Description", "Updated Manufacture",
                2.0, 2L, existingProduct.createdDate(), existingProduct.lastModifiedDate(), existingProduct.version());

        // When + Then
        testClient
                .put()
                .uri(PRODUCT_ROOT_ENDPOINT + "/" + productId)
                .bodyValue(updatedProduct)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(Product.class).value(actualProduct ->
                        assertProduct(actualProduct, updatedProduct)
                );
    }

    @Test
    @DisplayName("when delete request, then product should be deleted")
    void whenDeleteRequest_thenProductDeleted() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        var productId = productRepository.save(product).id();

        // When + Then
        testClient
                .delete()
                .uri(PRODUCT_ROOT_ENDPOINT + "/" + productId)
                .exchange()
                .expectStatus().isNoContent();
    }

    private void assertProduct(Product actualProduct, Product expectedProduct) {
        assertThat(actualProduct).isNotNull()
                .hasFieldOrPropertyWithValue("name", expectedProduct.name())
                .hasFieldOrPropertyWithValue("description", expectedProduct.description())
                .hasFieldOrPropertyWithValue("manufacturer", expectedProduct.manufacturer())
                .hasFieldOrPropertyWithValue("price", expectedProduct.price())
                .hasFieldOrPropertyWithValue("units", expectedProduct.units());
    }

}
