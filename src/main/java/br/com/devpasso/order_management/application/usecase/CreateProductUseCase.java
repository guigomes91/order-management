package br.com.devpasso.order_management.application.usecase;

import br.com.devpasso.order_management.application.dto.ProductRequest;
import br.com.devpasso.order_management.application.dto.ProductResponse;
import br.com.devpasso.order_management.application.mapper.ProductMapper;
import br.com.devpasso.order_management.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductDomainService productDomainService;
    private final ProductMapper productMapper;

    public ProductResponse execute(ProductRequest request) {
        return productMapper.toResponse(productDomainService.create(productMapper.toEntity(request)));
    }
}
