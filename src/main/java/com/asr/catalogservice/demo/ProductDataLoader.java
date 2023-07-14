package com.asr.catalogservice.demo;

import com.asr.catalogservice.domain.Product;
import com.asr.catalogservice.domain.ProductRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("testdata")
public class ProductDataLoader {
    private final ProductRepository repository;

    public ProductDataLoader(ProductRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadTestData() {
        repository.deleteAll();
        var product = Product.of("IPhone 14 Pro Max", "Apple IPhone 14 Pro Max with 256GB ", "Apple",
                1000.0, 10L);
        var product2 = Product.of("M1 Pro MacBook", "Apple 14 inches M1 Pro MacBook Pro", "Apple",
                2000.0, 5L);
        repository.saveAll(List.of(product, product2));
    }
}
