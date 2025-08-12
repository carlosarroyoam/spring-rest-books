package com.carlosarroyoam.rest.books.cart.dto;

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
public class UpdateCartItemRequestDto {
  @NotNull(message = "BookId should not be null")
  private Long bookId;

  @NotNull(message = "Quantity should not be null")
  @Min(value = 1, message = "Quantity should be min 1")
  private Integer quantity;
}
