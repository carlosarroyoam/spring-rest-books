package com.carlosarroyoam.rest.books.book.dto;

import com.carlosarroyoam.rest.books.book.entity.BookStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookSpecs {
  @Size(min = 10, max = 17, message = "Isbn should be between 10 and 17")
  private String isbn;

  @Size(max = 128, message = "Title should be max 128")
  private String title;

  @DecimalMin(value = "0.0", message = "Min price should be positive")
  private BigDecimal minPrice;

  @DecimalMin(value = "0.0", message = "Max price should be positive")
  private BigDecimal maxPrice;

  private Boolean isAvailableOnline;
  private BookStatus status;
  private List<Long> authorIds;
}