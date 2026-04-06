package com.carlosarroyoam.rest.books.order.dto;

import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderSpecsDto {
  @Size(max = 32, message = "Order number should be max 32")
  private String orderNumber;

  @DecimalMin(value = "0.0", message = "Min total should be positive")
  private BigDecimal minTotal;

  @DecimalMin(value = "0.0", message = "Max total should be positive")
  private BigDecimal maxTotal;

  @Size(max = 512, message = "Shipping address should be max 512")
  private String shippingAddress;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private LocalDateTime startDate;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private LocalDateTime endDate;

  private OrderStatus status;
  private Long customerId;
}
