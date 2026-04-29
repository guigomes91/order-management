package br.com.devpasso.order_management.application.usecase;

import br.com.devpasso.order_management.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteProductUseCase {

    private final ProductDomainService productDomainService;

    public void execute(UUID id) {
        productDomainService.delete(id);
    }
}
