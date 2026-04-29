package br.com.devpasso.order_management.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        Integer quantity,
        BigDecimal price
) {}
