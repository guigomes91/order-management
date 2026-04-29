package br.com.devpasso.order_management.application.usecase;

import br.com.devpasso.order_management.application.dto.OrderResponse;
import br.com.devpasso.order_management.application.dto.UpdateOrderStatusRequest;
import br.com.devpasso.order_management.application.mapper.OrderMapper;
import br.com.devpasso.order_management.domain.service.OrderDomainService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateOrderStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateOrderStatusUseCase.class);

    private final OrderDomainService orderDomainService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse execute(UUID id, UpdateOrderStatusRequest request) {
        log.info("Updating order status: orderId={}, newStatus={}", id, request.status());
        OrderResponse response = orderMapper.toResponse(orderDomainService.updateStatus(id, request.status()));
        log.info("Order status updated successfully: orderId={}, status={}", response.id(), response.status());
        return response;
    }
}
