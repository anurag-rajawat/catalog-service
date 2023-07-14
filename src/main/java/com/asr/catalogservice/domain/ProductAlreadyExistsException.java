package com.asr.catalogservice.domain;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String name) {
        super("Product with name '" + name + "' already exists.");
    }
}
