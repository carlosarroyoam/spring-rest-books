package com.carlosarroyoam.rest.books.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBookRequest {

	@NotBlank
	@Size(min = 3, max = 128)
	private String title;

	@NotBlank
	@Size(min = 3, max = 128)
	private String author;

	@Digits(integer = 3, fraction = 2)
	private BigDecimal price;

	@NotNull
	@PastOrPresent
	private LocalDate publishedAt;

	@NotNull
	private boolean isAvailableOnline;

}
