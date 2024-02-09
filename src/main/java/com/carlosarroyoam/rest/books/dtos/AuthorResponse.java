package com.carlosarroyoam.rest.books.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AuthorResponse {

	private Long id;
	private String firstName;
	private String lastName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}