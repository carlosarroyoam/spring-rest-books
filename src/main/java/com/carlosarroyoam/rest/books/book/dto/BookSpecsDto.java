package com.carlosarroyoam.rest.books.book.dto;

import com.carlosarroyoam.rest.books.core.validation.CommaSeparatedLong;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookSpecsDto {
  @Size(min = 10, max = 17, message = "Isbn should be between 10 and 17")
  private String isbn;

  @Size(max = 128, message = "Title should be max 128")
  private String title;

  @DecimalMin(value = "0.0", message = "Min price should be positive")
  private BigDecimal minPrice;

  @DecimalMin(value = "0.0", message = "Max price should be positive")
  private BigDecimal maxPrice;

  @CommaSeparatedLong(message = "AuthorIds should contain only valid ids")
  private String authorIds;

  private Boolean isAvailableOnline;
}
