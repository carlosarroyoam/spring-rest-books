package com.carlosarroyoam.rest.books.payment.dto;

import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreatePaymentRequest {
  @NotNull(message = "Order id should not be null")
  private Long orderId;

  @NotNull(message = "Method should not be null")
  private PaymentMethod method;
}
