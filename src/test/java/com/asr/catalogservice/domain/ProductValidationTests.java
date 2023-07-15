package com.asr.catalogservice.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ProductValidationTests {
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("when all fields are valid then, validation should succeed")
    void whenAllFieldsAreValidThen_validationSucceeds() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 1L);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations).isEmpty();
    }

    @NullAndEmptySource
    @ValueSource(strings = {"na"})
    @DisplayName("with invalid names")
    @ParameterizedTest(name = "with ''{0}'' name, validation should fail")
    void whenNameIsInvalidThen_validationFails(String name) {
        // Given
        var product = Product.of(name, "Description", "Manufacturer", 1.0, 1L);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations)
                .isNotEmpty()
                .hasSizeBetween(1, 2);
    }

    @NullAndEmptySource
    @ValueSource(strings = {"ma"})
    @DisplayName("with invalid manufacturers")
    @ParameterizedTest(name = "with ''{0}'' manufacturer, validation should fail")
    void whenManufacturerIsInvalidThen_validationFails(String manufacturer) {
        // Given
        var product = Product.of("Name", "Description", manufacturer, 1.0, 1L);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations)
                .isNotEmpty()
                .hasSizeBetween(1, 2);
    }

    @DisplayName("with invalid prices")
    @MethodSource("invalidPricesData")
    @ParameterizedTest(name = "''{0}'' validation should fail")
    void whenPriceIsInvalidThen_validationFails(String description, Double price, String expectedMessage) {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", price, 1L);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations)
                .singleElement()
                .extracting(ConstraintViolation::getMessage)
                .isEqualTo(expectedMessage);
    }

    static Stream<Arguments> invalidPricesData() {
        return Stream.of(
                arguments("with null price", null, "Product must have a price."),
                arguments("with price less than 1", 0.0, "Product price must be greater than zero"),
                arguments("with price greater than 1_000_000", 1_000_001.0, "Product price is too high")
        );
    }

    @Test
    @DisplayName("when units are greater than 10_000 then validation should fail")
    void whenUnitsAreGreaterThen10_000Then_validationFails() {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, 10_001L);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        // Then
        assertThat(violations)
                .singleElement()
                .extracting(ConstraintViolation::getMessage)
                .isEqualTo("Product must not have more than 10000 units.");
    }

    @NullSource
    @ValueSource(longs = {0L})
    @ParameterizedTest(name = "with ''{0}'' units, validation should fail")
    @DisplayName("when units are greater invalid then validation should fail")
    void whenUnitsInvalid_thenUnitsShouldDefaultsTo1And_validationSucceeds(Long units) {
        // Given
        var product = Product.of("Name", "Description", "Manufacturer", 1.0, units);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations).isEmpty();
    }
}
