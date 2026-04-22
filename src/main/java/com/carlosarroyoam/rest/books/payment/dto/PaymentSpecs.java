package com.carlosarroyoam.rest.books.payment.dto;

import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder
public class PaymentSpecs {
  @DecimalMin(value = "0.0", message = "Min amount should be positive")
  private BigDecimal minAmount;

  @DecimalMin(value = "0.0", message = "Max amount should be positive")
  private BigDecimal maxAmount;

  @Size(max = 128, message = "Transaction id should be max 128")
  private String transactionId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  private PaymentStatus status;
  private PaymentMethod method;
  private Long orderId;
}
