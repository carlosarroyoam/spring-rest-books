package com.carlosarroyoam.rest.books.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LoginResponse {
	private Long id;
	private String name;
	private String email;
	private String username;
	private Integer roleId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String accessToken;
}
