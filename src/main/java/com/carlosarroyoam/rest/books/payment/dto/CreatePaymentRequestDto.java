package com.carlosarroyoam.rest.books.payment.dto;

import com.carlosarroyoam.rest.books.orders.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequestDto {
  @NotNull(message = "Order_id should not be null")
  private Long orderId;

  @NotNull(message = "Method should not be null")
  private PaymentMethod method;
}
