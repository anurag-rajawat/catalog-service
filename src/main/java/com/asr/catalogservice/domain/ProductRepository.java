package com.asr.catalogservice.domain;

import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, String> {
    boolean existsByName(String name);
}
