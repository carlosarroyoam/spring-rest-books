package com.carlosarroyoam.rest.books.dtos;

import com.carlosarroyoam.rest.books.entities.Role;

import lombok.Data;

@Data
public class CreateUserRequest {

	private String firstName;
	private String lastName;
	private String email;
	private Role role;

}
