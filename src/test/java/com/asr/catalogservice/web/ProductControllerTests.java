package com.asr.catalogservice.web;

import com.asr.catalogservice.domain.Product;
import com.asr.catalogservice.domain.ProductNotFoundException;
import com.asr.catalogservice.domain.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTests {
    private static final String PRODUCT_URI = "/products";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("get all products, should return all products")
    void getAllProducts() throws Exception {
        // Given
        var product1 = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        var product2 = Product.of("Name2", "Description2", "Manufacturer2", 2.0, 2L);
        var products = List.of(product1, product2);
        given(productService.findAllProducts())
                .willReturn(products);
        var expected = """
                [{
                	"id": null,
                	"name": "Name",
                	"description": "Description",
                	"manufacturer": "Manufacturer",
                	"price": 1.0,
                	"units": 1,
                	"createdDate": null,
                	"lastModifiedDate": null,
                	"version": 0
                }, {
                	"id": null,
                	"name": "Name2",
                	"description": "Description2",
                	"manufacturer": "Manufacturer2",
                	"price": 2.0,
                	"units": 2,
                	"createdDate": null,
                	"lastModifiedDate": null,
                	"version": 0
                }]
                """;

        // When + Then
        mockMvc
                .perform(get(PRODUCT_URI))
                .andExpect(status().isOk())
                .andExpect(content().json(expected));
    }

    @Test
    @DisplayName("get product when not exists, should return 404")
    void getProduct_whenNotExists_shouldReturn404() throws Exception {
        // Given
        var productId = "64b13f81160f6f18fe1fdd49";
        given(productService.findProductById(productId))
                .willThrow(ProductNotFoundException.class);

        // When + Then
        mockMvc
                .perform(get(PRODUCT_URI + "/" + productId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("get product when exists, should return product")
    void getProduct_whenExists_shouldReturn404() throws Exception {
        // Given
        var productId = "64b13f81160f6f18fe1fdd49";
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);
        given(productService.findProductById(productId))
                .willReturn(product);
        var expected = """
                {
                 "name": "Name",
                 "description": "Description",
                 "manufacturer": "Manufacturer",
                 "price": 1.0,
                 "units": 1,
                 "version": 0
                }
                """;

        // When + Then
        mockMvc
                .perform(get(PRODUCT_URI + "/" + productId))
                .andExpect(status().isOk())
                .andExpect(content().json(expected, false));
    }

    @Test
    @DisplayName("add product, should add product")
    void addProduct() throws Exception {
        // Given
        var product = Product
                .of("Name", "Description", "Manufacturer", 1.0, 1L);
        given(productService.saveProduct(product))
                .willReturn(product);
        var productStr = """
                {
                "name": "Name",
                "description": "Description",
                "manufacturer": "Manufacturer",
                "price": 1.0,
                "units": 1
                }
                """;
        // When + Then
        mockMvc
                .perform(post(PRODUCT_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productStr)
                ).andExpect(status().isCreated())
                .andExpect(content().json(productStr));
    }

    @Test
    @DisplayName("delete product when exists, should delete")
    void deleteProduct() throws Exception {
        // Given
        var productId = "64b13f81160f6f18fe1fdd49s";

        // When + then
        mockMvc
                .perform(delete(PRODUCT_URI + "/" + productId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("update product when exists, should update")
    void updateProduct_whenExists() throws Exception {
        // Given
        var productId = "64b13f81160f6f18fe1fdd49s";

        var updatedProduct = new Product(productId, "Name", "Updated Description", "Updated Manufacturer",
                2.0, 2L, null, null, 2);
        given(productService.updateProduct(productId, updatedProduct))
                .willReturn(updatedProduct);
        var updatedProductStr = """
                {
                "id": "%s",
                "name": "Name",
                "description": "Updated Description",
                "manufacturer": "Updated Manufacturer",
                "price": 2.0,
                "units": 2,
                "version": 2
                }
                """.formatted(productId);

        var expected = """
                {
                 "id": "%s",
                 "name": "Name",
                 "description": "Updated Description",
                 "manufacturer": "Updated Manufacturer",
                 "price": 2.0,
                 "units": 2,
                 "version": 2,
                 "createdDate": null,
                 "lastModifiedDate": null
                }
                """.formatted(productId);

        // When + Then
        mockMvc
                .perform(put(PRODUCT_URI + "/" + productId)
                        .content(updatedProductStr)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isAccepted())
                .andExpect(content().json(expected, true));
    }
}
