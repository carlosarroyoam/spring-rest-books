package com.carlosarroyoam.rest.books.orders;

import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.orders.dto.CreateOrderRequestDto;
import com.carlosarroyoam.rest.books.orders.dto.OrderDto;
import com.carlosarroyoam.rest.books.orders.dto.UpdateOrderRequestDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
  public ResponseEntity<PagedResponseDto<OrderDto>> findAll(
      @PageableDefault(page = 0, size = 25) Pageable pageable) {
    PagedResponseDto<OrderDto> orders = orderService.findAll(pageable);
    return ResponseEntity.ok(orders);
  }

  @GetMapping(value = "/{orderId}", produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<OrderDto> findById(@PathVariable Long orderId) {
    OrderDto orderById = orderService.findById(orderId);
    return ResponseEntity.ok(orderById);
  }

  @PostMapping(consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> create(@Valid @RequestBody CreateOrderRequestDto requestDto,
      UriComponentsBuilder builder) {
    OrderDto createdOrder = orderService.create(requestDto);
    UriComponents uriComponents = builder.path("/orders/{orderId}")
        .buildAndExpand(createdOrder.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  @PutMapping(value = "/{orderId}", consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> update(@PathVariable Long orderId,
      @Valid @RequestBody UpdateOrderRequestDto requestDto) {
    orderService.update(orderId, requestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{orderId}")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> deleteById(@PathVariable Long orderId) {
    orderService.deleteById(orderId);
    return ResponseEntity.noContent().build();
  }
}
