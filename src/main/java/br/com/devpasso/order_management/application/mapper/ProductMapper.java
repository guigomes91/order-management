package br.com.devpasso.order_management.application.mapper;

import br.com.devpasso.order_management.application.dto.ProductRequest;
import br.com.devpasso.order_management.application.dto.ProductResponse;
import br.com.devpasso.order_management.domain.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }

    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .name(request.name())
                .price(request.price())
                .stock(request.stock())
                .build();
    }
}
