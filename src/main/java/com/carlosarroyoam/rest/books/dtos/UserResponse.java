package com.carlosarroyoam.rest.books.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponse {

	private Long id;
	private String name;
	private Byte age;
	private String email;
	private String username;
	private boolean isActive;
	private Integer roleId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
