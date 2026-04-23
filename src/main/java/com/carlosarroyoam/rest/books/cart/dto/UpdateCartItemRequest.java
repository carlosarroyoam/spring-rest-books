package com.carlosarroyoam.rest.books.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdateCartItemRequest {
  @NotNull(message = "BookId should not be null")
  private Long bookId;

  @NotNull(message = "Quantity should not be null")
  @Min(value = 1, message = "Quantity should be min 1")
  private Integer quantity;
}
