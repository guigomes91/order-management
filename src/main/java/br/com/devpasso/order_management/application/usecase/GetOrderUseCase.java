package br.com.devpasso.order_management.application.usecase;

import br.com.devpasso.order_management.application.dto.OrderResponse;
import br.com.devpasso.order_management.application.mapper.OrderMapper;
import br.com.devpasso.order_management.domain.service.OrderDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetOrderUseCase {

    private final OrderDomainService orderDomainService;
    private final OrderMapper orderMapper;

    public OrderResponse execute(UUID id) {
        return orderMapper.toResponse(orderDomainService.findById(id));
    }
}
