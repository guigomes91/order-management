package br.com.devpasso.order_management.application.usecase;

import br.com.devpasso.order_management.application.dto.ProductResponse;
import br.com.devpasso.order_management.application.mapper.ProductMapper;
import br.com.devpasso.order_management.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListProductsUseCase {

    private final ProductDomainService productDomainService;
    private final ProductMapper productMapper;

    public List<ProductResponse> execute() {
        return productDomainService.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }
}
