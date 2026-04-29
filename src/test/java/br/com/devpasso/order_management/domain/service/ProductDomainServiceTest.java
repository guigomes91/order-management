package br.com.devpasso.order_management.domain.service;

import br.com.devpasso.order_management.domain.exception.InsufficientStockException;
import br.com.devpasso.order_management.domain.exception.ProductNotFoundException;
import br.com.devpasso.order_management.domain.model.Product;
import br.com.devpasso.order_management.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDomainServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductDomainService productDomainService;

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create: should save and return the product")
    void create_success() {
        Product input = Product.builder()
                .name("Notebook")
                .price(new BigDecimal("3500.00"))
                .stock(10)
                .build();
        Product saved = Product.builder()
                .id(UUID.randomUUID())
                .name("Notebook")
                .price(new BigDecimal("3500.00"))
                .stock(10)
                .build();

        when(productRepository.save(input)).thenReturn(saved);

        Product result = productDomainService.create(input);

        assertThat(result).isEqualTo(saved);
        verify(productRepository).save(input);
    }

    @Test
    @DisplayName("create: should throw IllegalArgumentException when stock is negative")
    void create_negativeStock_throwsIllegalArgument() {
        Product input = Product.builder()
                .name("Notebook")
                .price(new BigDecimal("3500.00"))
                .stock(-1)
                .build();

        assertThatThrownBy(() -> productDomainService.create(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");

        verifyNoInteractions(productRepository);
    }

    // -------------------------------------------------------------------------
    // findById()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById: should return the product when it exists")
    void findById_found() {
        UUID id = UUID.randomUUID();
        Product product = Product.builder().id(id).name("Mouse").price(new BigDecimal("150.00")).stock(5).build();

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product result = productDomainService.findById(id);

        assertThat(result).isEqualTo(product);
    }

    @Test
    @DisplayName("findById: should throw ProductNotFoundException when product is missing")
    void findById_notFound_throwsProductNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDomainService.findById(id))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    // -------------------------------------------------------------------------
    // findAll()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll: should return all products from repository")
    void findAll_returnsAllProducts() {
        List<Product> products = List.of(
                Product.builder().id(UUID.randomUUID()).name("Pen").price(new BigDecimal("2.50")).stock(100).build(),
                Product.builder().id(UUID.randomUUID()).name("Desk").price(new BigDecimal("800.00")).stock(3).build()
        );
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productDomainService.findAll();

        assertThat(result).hasSize(2).containsExactlyElementsOf(products);
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update: should update all fields and return saved product")
    void update_success() {
        UUID id = UUID.randomUUID();
        Product existing = Product.builder().id(id).name("Old Name").price(new BigDecimal("10.00")).stock(5).build();
        Product updated = Product.builder().name("New Name").price(new BigDecimal("20.00")).stock(15).build();
        Product saved = Product.builder().id(id).name("New Name").price(new BigDecimal("20.00")).stock(15).build();

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(saved);

        Product result = productDomainService.update(id, updated);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPrice()).isEqualByComparingTo("20.00");
        assertThat(result.getStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("update: should throw IllegalArgumentException when updated stock is negative")
    void update_negativeStock_throwsIllegalArgument() {
        UUID id = UUID.randomUUID();
        Product updated = Product.builder().name("X").price(new BigDecimal("5.00")).stock(-3).build();

        assertThatThrownBy(() -> productDomainService.update(id, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");

        verifyNoInteractions(productRepository);
    }

    // -------------------------------------------------------------------------
    // deductStock()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deductStock: should subtract quantity and save the product")
    void deductStock_success() {
        UUID id = UUID.randomUUID();
        Product product = Product.builder().id(id).name("Keyboard").price(new BigDecimal("250.00")).stock(10).build();

        productDomainService.deductStock(product, 3);

        assertThat(product.getStock()).isEqualTo(7);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("deductStock: should throw InsufficientStockException when stock is too low")
    void deductStock_insufficient_throwsInsufficientStockException() {
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Monitor")
                .price(new BigDecimal("1200.00"))
                .stock(2)
                .build();

        assertThatThrownBy(() -> productDomainService.deductStock(product, 5))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Monitor")
                .hasMessageContaining("requested 5")
                .hasMessageContaining("available 2");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("deductStock: should throw IllegalArgumentException when quantity is negative")
    void deductStock_negativeQuantity_throwsIllegalArgument() {
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Headset")
                .price(new BigDecimal("300.00"))
                .stock(10)
                .build();

        assertThatThrownBy(() -> productDomainService.deductStock(product, -2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");

        verify(productRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("delete: should find the product and delete it by id")
    void delete_success() {
        UUID id = UUID.randomUUID();
        Product product = Product.builder().id(id).name("Chair").price(new BigDecimal("500.00")).stock(4).build();

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        productDomainService.delete(id);

        verify(productRepository).deleteById(id);
    }

    @Test
    @DisplayName("delete: should throw ProductNotFoundException when product does not exist")
    void delete_notFound_throwsProductNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDomainService.delete(id))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }
}
