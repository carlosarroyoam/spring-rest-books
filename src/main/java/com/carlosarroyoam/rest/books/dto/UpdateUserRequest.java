package com.carlosarroyoam.rest.books.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

	@NotBlank
	@Size(min = 3, max = 128)
	private String name;

	@NotNull
	private Byte age;

}
