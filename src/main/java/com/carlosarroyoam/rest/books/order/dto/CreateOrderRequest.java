package com.carlosarroyoam.rest.books.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateOrderRequest {
  @NotNull(message = "Customer id should not be null")
  private Long customerId;

  @NotBlank(message = "Shipping address should not be blank")
  @Size(min = 10, max = 512, message = "Shipping_address should be between 10 and 512")
  private String shippingAddress;

  @NotBlank(message = "Billing address should not be blank")
  @Size(min = 10, max = 512, message = "Billing address should be between 10 and 512")
  private String billingAddress;

  @Size(max = 1000, message = "Notes should be between 0 and 1000")
  private String notes;

  @Valid
  @NotEmpty(message = "Items should not be empty")
  private List<CreateOrderItemRequest> items;
}
