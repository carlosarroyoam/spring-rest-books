package com.carlosarroyoam.rest.books.dtos;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class BookResponse {

	private Long id;
	private String title;
	private List<AuthorResponse> authors;
	private Double price;
	private LocalDate publishedAt;
	private boolean isAvailableOnline;

}
