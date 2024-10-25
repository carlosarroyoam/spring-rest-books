package com.carlosarroyoam.rest.books.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBookRequest {

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
