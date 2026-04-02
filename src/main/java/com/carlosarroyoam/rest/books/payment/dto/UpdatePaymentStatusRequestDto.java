package com.carlosarroyoam.rest.books.payment.dto;

import com.carlosarroyoam.rest.books.orders.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePaymentStatusRequestDto {
  @NotNull(message = "Status should not be null")
  private PaymentStatus status;
}
