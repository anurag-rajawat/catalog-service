package com.asr.catalogservice.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
public record Product(
        @Id
        String id,

        @NotBlank(message = "Product must have a name.")
        @Size(min = 3, message = "Product name must be at least 3 characters long.")
        String name,

        String description,

        @NotBlank(message = "Product must have a manufacturer.")
        @Size(min = 3, message = "Product manufacturer name must be at least 3 characters long.")
        String manufacturer,

        @NotNull(message = "Product must have a price.")
        @Min(value = 1, message = "Product price must be greater than zero")
        @Max(value = 1_000_000, message = "Product price is too high")
        Double price,

        @Max(value = 10_000, message = "Product must not have more than 10000 units.")
        Long units,

        @CreatedDate
        Instant createdDate,

        @LastModifiedDate
        Instant lastModifiedDate,

        @Version
        int version
) {
    public static Product of(String name, String description, String manufacturer, Double price, Long units) {
        if (units == null || units == 0) {
            units = 1L;
        }
        return new Product(null, name, description, manufacturer, price, units, null, null, 0);
    }
}
