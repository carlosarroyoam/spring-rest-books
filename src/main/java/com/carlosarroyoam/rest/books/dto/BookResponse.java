package com.carlosarroyoam.rest.books.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class BookResponse {

	private Long id;
	private String isbn;
	private String title;
	private List<AuthorResponse> authors;
	private Double price;
	private boolean isAvailableOnline;
	private LocalDate publishedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
