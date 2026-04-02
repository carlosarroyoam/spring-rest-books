package com.carlosarroyoam.rest.books.orders.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderRequestDto {
  @NotBlank(message = "Shipping_address should not be blank")
  @Size(min = 10, max = 512, message = "Shipping_address should be between 10 and 512")
  private String shippingAddress;

  @NotBlank(message = "Billing_address should not be blank")
  @Size(min = 10, max = 512, message = "Billing_address should be between 10 and 512")
  private String billingAddress;

  @Size(max = 1000, message = "Notes should be between 0 and 1000")
  private String notes;
}
