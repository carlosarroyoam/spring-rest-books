package com.carlosarroyoam.rest.books.dtos;

import lombok.Data;

@Data
public class CreateUserRequest {

	private String firstName;
	private String lastName;
	private String email;
	private Long roleId;

}
