package com.carlosarroyoam.rest.books.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookRequestDto {
  @NotBlank(message = "ISBN should not be blank")
  @Size(min = 10, max = 17, message = "Isbn should be between 10 and 17")
  private String isbn;

  @NotBlank(message = "Title should not be blank")
  @Size(min = 3, max = 128, message = "Title should be between 3 and 128")
  private String title;

  @NotBlank(message = "Cover_url should not be blank")
  @Size(min = 3, max = 512, message = "Cover_url should be between 3 and 128")
  private String coverUrl;

  @NotNull(message = "Price should not be null")
  @Digits(integer = 5, fraction = 2, message = "Price should have max 5 integral digits and max 2 fractional digits")
  private BigDecimal price;

  @NotNull(message = "Is_available_online should not be null")
  private Boolean isAvailableOnline;

  @NotNull(message = "Published_at should not be null")
  @PastOrPresent(message = "Published_at should be a date in past or present")
  private LocalDate publishedAt;
}
