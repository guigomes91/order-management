package br.com.devpasso.order_management.domain.service;

import br.com.devpasso.order_management.domain.exception.InsufficientStockException;
import br.com.devpasso.order_management.domain.exception.ProductNotFoundException;
import br.com.devpasso.order_management.domain.model.Product;
import br.com.devpasso.order_management.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductDomainService {

    private static final Logger log = LoggerFactory.getLogger(ProductDomainService.class);

    private final ProductRepository productRepository;

    public Product create(Product product) {
        validateStockNotNegative(product.getStock(), "Stock");
        Product saved = productRepository.save(product);
        log.info("Product created: productId={}, name={}, stock={}",
                saved.getId(), saved.getName(), saved.getStock());
        return saved;
    }

    public Product findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found: productId={}", id);
                    return new ProductNotFoundException(id);
                });
    }

    public List<Product> findAll() {
        List<Product> products = productRepository.findAll();
        log.debug("Fetched {} product(s)", products.size());
        return products;
    }

    public Product update(UUID id, Product updated) {
        validateStockNotNegative(updated.getStock(), "Stock");
        Product existing = findById(id);
        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        Product saved = productRepository.save(existing);
        log.info("Product updated: productId={}, name={}, stock={}",
                saved.getId(), saved.getName(), saved.getStock());
        return saved;
    }

    public void deductStock(Product product, int quantity) {
        validateStockNotNegative(quantity, "Quantity");
        if (product.getStock() < quantity) {
            log.warn("Insufficient stock: productId={}, requested={}, available={}",
                    product.getId(), quantity, product.getStock());
            throw new InsufficientStockException(product.getName(), quantity, product.getStock());
        }
        int stockBefore = product.getStock();
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        log.info("Stock deducted: productId={}, deducted={}, stockBefore={}, stockAfter={}",
                product.getId(), quantity, stockBefore, product.getStock());
    }

    public void delete(UUID id) {
        findById(id);
        productRepository.deleteById(id);
        log.info("Product deleted: productId={}", id);
    }

    private void validateStockNotNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }
}
