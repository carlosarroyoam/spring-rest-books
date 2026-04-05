package com.carlosarroyoam.rest.books.payment.dto;

import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentSpecsDto {
  @DecimalMin(value = "0.0", message = "Min amount should be positive")
  private BigDecimal minAmount;

  @DecimalMin(value = "0.0", message = "Max amount should be positive")
  private BigDecimal maxAmount;

  @Size(max = 128, message = "Transaction id should be max 128")
  private String transactionId;

  private PaymentStatus status;
  private PaymentMethod method;
  private Long orderId;
}