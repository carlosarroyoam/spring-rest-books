package com.carlosarroyoam.rest.books.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateOrderItemRequest {
  @NotNull(message = "Book id should not be null")
  private Long bookId;

  @NotNull(message = "Quantity should not be null")
  @Min(value = 1, message = "Quantity should be greater than 0")
  private Integer quantity;
}
