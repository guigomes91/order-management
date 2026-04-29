package br.com.devpasso.order_management.application.usecase;

import br.com.devpasso.order_management.application.dto.OrderItemRequest;
import br.com.devpasso.order_management.application.dto.OrderRequest;
import br.com.devpasso.order_management.application.dto.OrderResponse;
import br.com.devpasso.order_management.application.mapper.OrderMapper;
import br.com.devpasso.order_management.domain.model.Order;
import br.com.devpasso.order_management.domain.model.OrderItem;
import br.com.devpasso.order_management.domain.model.OrderStatus;
import br.com.devpasso.order_management.domain.model.Product;
import br.com.devpasso.order_management.domain.service.OrderDomainService;
import br.com.devpasso.order_management.domain.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderUseCase.class);

    private final OrderDomainService orderDomainService;
    private final ProductDomainService productDomainService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse execute(OrderRequest request) {
        log.info("Creating order with {} item(s)", request.items().size());

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productDomainService.findById(itemRequest.productId());
            log.debug("Processing item: productId={}, quantity={}, unitPrice={}",
                    product.getId(), itemRequest.quantity(), product.getPrice());

            productDomainService.deductStock(product, itemRequest.quantity());

            BigDecimal linePrice = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.quantity()));

            items.add(OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .quantity(itemRequest.quantity())
                    .price(linePrice)
                    .build());

            total = total.add(linePrice);
        }

        order.getItems().addAll(items);
        order.setTotalAmount(total);

        OrderResponse response = orderMapper.toResponse(orderDomainService.create(order));
        log.info("Order created successfully: orderId={}, totalAmount={}, status={}",
                response.id(), response.totalAmount(), response.status());
        return response;
    }
}
