package com.carlosarroyoam.rest.books.order.dto;

import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Builder
public class OrderSpecs {
  @Size(max = 32, message = "Order number should be max 32")
  private String orderNumber;

  @Size(max = 512, message = "Shipping address should be max 512")
  private String shippingAddress;

  @DecimalMin(value = "0.0", message = "Min total should be positive")
  private BigDecimal minTotal;

  @DecimalMin(value = "0.0", message = "Max total should be positive")
  private BigDecimal maxTotal;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  private OrderStatus status;
  private Long customerId;
}