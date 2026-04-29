package br.com.devpasso.order_management.application.dto;

import br.com.devpasso.order_management.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "Status is required")
        OrderStatus status
) {}
