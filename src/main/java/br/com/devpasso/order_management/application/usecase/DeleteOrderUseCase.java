package br.com.devpasso.order_management.application.usecase;

import br.com.devpasso.order_management.domain.service.OrderDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteOrderUseCase {

    private final OrderDomainService orderDomainService;

    public void execute(UUID id) {
        orderDomainService.delete(id);
    }
}
