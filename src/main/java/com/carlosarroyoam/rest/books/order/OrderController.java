package com.carlosarroyoam.rest.books.order;

import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderRequest;
import com.carlosarroyoam.rest.books.order.dto.OrderResponse;
import com.carlosarroyoam.rest.books.order.dto.OrderSpecs;
import com.carlosarroyoam.rest.books.order.dto.UpdateOrderRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping(produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<PagedResponse<OrderResponse>> findAll(
      @Valid @ModelAttribute OrderSpecs orderSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<OrderResponse> orders = orderService.findAll(orderSpecs, pageable);
    return ResponseEntity.ok(orders);
  }

  @GetMapping(value = "/{orderId}", produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<OrderResponse> findById(@PathVariable Long orderId) {
    OrderResponse orderById = orderService.findById(orderId);
    return ResponseEntity.ok(orderById);
  }

  @PostMapping(consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> create(
      @Valid @RequestBody CreateOrderRequest request, UriComponentsBuilder builder) {
    OrderResponse createdOrder = orderService.create(request);
    UriComponents uriComponents =
        builder.path("/orders/{orderId}").buildAndExpand(createdOrder.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  @PutMapping(value = "/{orderId}", consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> update(
      @PathVariable Long orderId, @Valid @RequestBody UpdateOrderRequest request) {
    orderService.update(orderId, request);
    return ResponseEntity.noContent().build();
  }
}
