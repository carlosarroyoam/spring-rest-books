package com.carlosarroyoam.rest.books.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponse {

	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private boolean isActive = false;
	private Integer roleId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
