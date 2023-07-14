package com.asr.catalogservice.domain;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Iterable<Product> findAllProducts() {
        return repository.findAll();
    }

    public Product findProductById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product saveProduct(Product product) {
        if (repository.existsByName(product.name())) {
            throw new ProductAlreadyExistsException(product.name());
        }
        // Number of units should default to 1 if not specified.
        if (product.units() == null || product.units() == 0) {
            product = Product.of(product.name(), product.description(), product.manufacturer(), product.price(), product.units());
        }
        return repository.save(product);
    }

    public void deleteProductById(String id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
    }

    // TODO: Rethink about refactor, whether only update product details or create new one if not exists
    public Product updateProduct(String id, Product product) {
        return repository.findById(id)
                .map(existingProduct -> {
                    var productToUpdate = new Product(
                            existingProduct.id(),
                            existingProduct.name(),
                            product.description(),
                            product.manufacturer(),
                            product.price(),
                            product.units(),
                            existingProduct.createdDate(),
                            existingProduct.lastModifiedDate(),
                            existingProduct.version()
                    );
                    return repository.save(productToUpdate);
                })
                .orElseGet(() -> saveProduct(product));
    }

}
