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
public class UpdateBookRequestDto {
  @NotBlank
  @Size(min = 3, max = 128)
  private String title;

  @NotBlank
  @Size(min = 3, max = 512)
  private String coverUrl;

  @NotBlank
  @Size(min = 10, max = 17)
  private String isbn;

  @NotNull
  @Digits(integer = 5, fraction = 2)
  private BigDecimal price;

  @NotNull
  private Boolean isAvailableOnline;

  @NotNull
  @PastOrPresent
  private LocalDate publishedAt;
}
