package com.carlosarroyoam.rest.books.dtos;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.Data;

@Data
public class AppExceptionResponse {

	private String message;
	private String error;
	private List<String> details;
	private int status;
	private String path;
	private ZonedDateTime timestamp;

}
