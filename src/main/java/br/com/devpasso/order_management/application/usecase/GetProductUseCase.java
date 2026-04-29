package br.com.devpasso.order_management.application.usecase;

import br.com.devpasso.order_management.application.dto.ProductResponse;
import br.com.devpasso.order_management.application.mapper.ProductMapper;
import br.com.devpasso.order_management.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetProductUseCase {

    private final ProductDomainService productDomainService;
    private final ProductMapper productMapper;

    public ProductResponse execute(UUID id) {
        return productMapper.toResponse(productDomainService.findById(id));
    }
}
