package br.com.devpasso.order_management.domain.service;

import br.com.devpasso.order_management.domain.exception.InvalidOrderStatusTransitionException;
import br.com.devpasso.order_management.domain.model.Order;
import br.com.devpasso.order_management.domain.model.OrderStatus;
import br.com.devpasso.order_management.domain.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderDomainService {

    private static final Logger log = LoggerFactory.getLogger(OrderDomainService.class);

    private final OrderRepository orderRepository;

    public Order create(Order order) {
        Order saved = orderRepository.save(order);
        log.info("Order persisted: orderId={}, status={}, totalAmount={}",
                saved.getId(), saved.getStatus(), saved.getTotalAmount());
        return saved;
    }

    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order not found: orderId={}", id);
                    return new EntityNotFoundException("Order not found: " + id);
                });
    }

    public List<Order> findAll() {
        List<Order> orders = orderRepository.findAll();
        log.debug("Fetched {} order(s)", orders.size());
        return orders;
    }

    public Order updateStatus(UUID id, OrderStatus newStatus) {
        Order order = findById(id);
        OrderStatus previousStatus = order.getStatus();
        validateTransition(previousStatus, newStatus);
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Order status changed: orderId={}, from={}, to={}", id, previousStatus, newStatus);
        return saved;
    }

    public void delete(UUID id) {
        findById(id);
        orderRepository.deleteById(id);
        log.info("Order deleted: orderId={}", id);
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.CANCELED) {
            throw new InvalidOrderStatusTransitionException(current, next);
        }
        if (current == OrderStatus.FAILED && next == OrderStatus.PAID) {
            throw new InvalidOrderStatusTransitionException(current, next);
        }
    }
}
