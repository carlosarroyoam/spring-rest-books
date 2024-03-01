package com.carlosarroyoam.rest.books.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAuthorRequest {

	@NotBlank
	@Size(min = 3, max = 128)
	private String name;

}
