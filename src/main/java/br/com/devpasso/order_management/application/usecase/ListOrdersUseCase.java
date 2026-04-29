package br.com.devpasso.order_management.application.usecase;

import br.com.devpasso.order_management.application.dto.OrderResponse;
import br.com.devpasso.order_management.application.mapper.OrderMapper;
import br.com.devpasso.order_management.domain.service.OrderDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListOrdersUseCase {

    private final OrderDomainService orderDomainService;
    private final OrderMapper orderMapper;

    public List<OrderResponse> execute() {
        return orderDomainService.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }
}
