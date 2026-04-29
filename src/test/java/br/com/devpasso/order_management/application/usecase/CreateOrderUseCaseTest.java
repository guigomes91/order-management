package br.com.devpasso.order_management.application.usecase;

import br.com.devpasso.order_management.application.dto.OrderItemRequest;
import br.com.devpasso.order_management.application.dto.OrderItemResponse;
import br.com.devpasso.order_management.application.dto.OrderRequest;
import br.com.devpasso.order_management.application.dto.OrderResponse;
import br.com.devpasso.order_management.application.mapper.OrderMapper;
import br.com.devpasso.order_management.domain.exception.InsufficientStockException;
import br.com.devpasso.order_management.domain.exception.ProductNotFoundException;
import br.com.devpasso.order_management.domain.model.Order;
import br.com.devpasso.order_management.domain.model.OrderStatus;
import br.com.devpasso.order_management.domain.model.Product;
import br.com.devpasso.order_management.domain.service.OrderDomainService;
import br.com.devpasso.order_management.domain.service.ProductDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private OrderDomainService orderDomainService;

    @Mock
    private ProductDomainService productDomainService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Product buildProduct(UUID id, String name, BigDecimal price, int stock) {
        return Product.builder().id(id).name(name).price(price).stock(stock).build();
    }

    private OrderResponse buildOrderResponse(UUID orderId, BigDecimal total) {
        return new OrderResponse(orderId, OrderStatus.PENDING, total, LocalDateTime.now(), List.of());
    }

    // -------------------------------------------------------------------------
    // execute()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute: should create order with correct total for a single item")
    void execute_singleItem_correctTotal() {
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Product product = buildProduct(productId, "Notebook", new BigDecimal("3500.00"), 5);
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(productId, 2)));
        OrderResponse expectedResponse = buildOrderResponse(orderId, new BigDecimal("7000.00"));

        when(productDomainService.findById(productId)).thenReturn(product);
        when(orderDomainService.create(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(expectedResponse);

        OrderResponse result = createOrderUseCase.execute(request);

        assertThat(result).isEqualTo(expectedResponse);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDomainService).create(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(capturedOrder.getTotalAmount()).isEqualByComparingTo("7000.00");
        assertThat(capturedOrder.getItems()).hasSize(1);
        assertThat(capturedOrder.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(capturedOrder.getItems().get(0).getPrice()).isEqualByComparingTo("7000.00");
    }

    @Test
    @DisplayName("execute: should aggregate total correctly for multiple items")
    void execute_multipleItems_totalIsSum() {
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        Product p1 = buildProduct(productId1, "Mouse", new BigDecimal("150.00"), 10);
        Product p2 = buildProduct(productId2, "Keyboard", new BigDecimal("250.00"), 10);

        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(productId1, 2),  // 300.00
                new OrderItemRequest(productId2, 1)   // 250.00
        ));

        when(productDomainService.findById(productId1)).thenReturn(p1);
        when(productDomainService.findById(productId2)).thenReturn(p2);
        when(orderDomainService.create(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            return buildOrderResponse(UUID.randomUUID(), o.getTotalAmount());
        });

        OrderResponse result = createOrderUseCase.execute(request);

        assertThat(result.totalAmount()).isEqualByComparingTo("550.00");
    }

    @Test
    @DisplayName("execute: should call deductStock once per item")
    void execute_callsDeductStockForEachItem() {
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        Product p1 = buildProduct(productId1, "Monitor", new BigDecimal("800.00"), 5);
        Product p2 = buildProduct(productId2, "Webcam", new BigDecimal("200.00"), 3);

        OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(productId1, 1),
                new OrderItemRequest(productId2, 2)
        ));

        when(productDomainService.findById(productId1)).thenReturn(p1);
        when(productDomainService.findById(productId2)).thenReturn(p2);
        when(orderDomainService.create(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            return buildOrderResponse(UUID.randomUUID(), o.getTotalAmount());
        });

        createOrderUseCase.execute(request);

        verify(productDomainService).deductStock(p1, 1);
        verify(productDomainService).deductStock(p2, 2);
    }

    @Test
    @DisplayName("execute: should set order status to PENDING")
    void execute_orderHasPendingStatus() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId, "Desk", new BigDecimal("600.00"), 10);
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(productId, 1)));

        when(productDomainService.findById(productId)).thenReturn(product);
        when(orderDomainService.create(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(buildOrderResponse(UUID.randomUUID(), new BigDecimal("600.00")));

        createOrderUseCase.execute(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderDomainService).create(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("execute: should propagate ProductNotFoundException when a product is not found")
    void execute_productNotFound_throwsProductNotFoundException() {
        UUID productId = UUID.randomUUID();
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(productId, 1)));

        when(productDomainService.findById(productId)).thenThrow(new ProductNotFoundException(productId));

        assertThatThrownBy(() -> createOrderUseCase.execute(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(productId.toString());

        verify(orderDomainService, never()).create(any());
    }

    @Test
    @DisplayName("execute: should propagate InsufficientStockException when stock is too low")
    void execute_insufficientStock_throwsInsufficientStockException() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId, "GPU", new BigDecimal("5000.00"), 1);
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(productId, 3)));

        when(productDomainService.findById(productId)).thenReturn(product);
        doThrow(new InsufficientStockException("GPU", 3, 1))
                .when(productDomainService).deductStock(product, 3);

        assertThatThrownBy(() -> createOrderUseCase.execute(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("GPU");

        verify(orderDomainService, never()).create(any());
    }

    @Test
    @DisplayName("execute: should link each OrderItem back to the Order")
    void execute_itemsLinkedToOrder() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId, "Chair", new BigDecimal("400.00"), 10);
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(productId, 2)));

        when(productDomainService.findById(productId)).thenReturn(product);
        when(orderDomainService.create(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(buildOrderResponse(UUID.randomUUID(), new BigDecimal("800.00")));

        createOrderUseCase.execute(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderDomainService).create(captor.capture());
        Order order = captor.getValue();

        assertThat(order.getItems()).allSatisfy(item ->
                assertThat(item.getOrder()).isSameAs(order)
        );
    }
}
