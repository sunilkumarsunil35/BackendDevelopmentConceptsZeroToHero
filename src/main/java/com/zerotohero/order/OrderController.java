package com.zerotohero.order;

import com.zerotohero.order.OrderDtos.OrderResponse;
import com.zerotohero.order.OrderDtos.PlaceOrderRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Modules 10, 13 and 19: orders belong to the signed-in user, taken from the JWT. */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders")
@SecurityRequirement(name = "bearer")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> place(Principal principal,
                                               @RequestHeader(name = "Idempotency-Key", required = false) String key,
                                               @Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse order = key == null
                ? service.placeOrder(principal.getName(), request)
                : service.placeOrderIdempotent(principal.getName(), key, request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(order);
    }

    @GetMapping
    public List<Long> mine(Principal principal) {
        return service.idsFor(principal.getName());
    }

    @GetMapping("/{id}")
    public OrderResponse get(Principal principal, @PathVariable Long id) {
        return service.get(id, principal.getName());
    }
}
