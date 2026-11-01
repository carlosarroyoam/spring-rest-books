package com.carlosarroyoam.rest.books.payment.dto;

import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Datos requeridos para actualizar el estado de un pago. */
@Getter
@Setter
@Builder
public class UpdatePaymentStatusRequest {
  @NotNull(message = "Status should not be null")
  private PaymentStatus status;
}
