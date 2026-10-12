package com.zerotohero.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Request and response shapes for the orders API. */
public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderLine(@NotNull Long bookId, @Min(1) @Max(10) int quantity) {
    }

    // @Valid on the list element cascades validation into each line.
    public record PlaceOrderRequest(@NotEmpty @Size(max = 20) List<@Valid OrderLine> items) {
    }

    public record OrderItemResponse(Long bookId, String title, int quantity, BigDecimal unitPrice) {
    }

    public record OrderResponse(Long id, String customer, OrderStatus status, BigDecimal total,
                                Instant createdAt, List<OrderItemResponse> items) {

        static OrderResponse from(Order order) {
            return new OrderResponse(order.getId(), order.getCustomer(), order.getStatus(), order.getTotal(),
                    order.getCreatedAt(), order.getItems().stream()
                    .map(i -> new OrderItemResponse(i.getBook().getId(), i.getBook().getTitle(), i.getQuantity(),
                            i.getUnitPrice()))
                    .toList());
        }
    }
}
