package com.carlosarroyoam.rest.books.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderItemRequestDto {
  @NotNull(message = "Book id should not be null")
  private Long bookId;

  @NotNull(message = "Quantity should not be null")
  @Min(value = 1, message = "Quantity should be greater than 0")
  private Integer quantity;
}
