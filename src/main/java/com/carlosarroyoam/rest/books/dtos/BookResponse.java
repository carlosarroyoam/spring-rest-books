package com.carlosarroyoam.rest.books.dtos;

import java.time.LocalDate;

import lombok.Data;

@Data
public class BookResponse {

	private Long id;
	private String title;
	private String author;
	private Double price;
	private LocalDate publishedAt;
	private boolean isAvailableOnline;

}
