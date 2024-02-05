package com.carlosarroyoam.bookservice.dtos;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class AppExceptionResponse {

	private String message;
	private String error;
	private int status;
	private String path;
	private ZonedDateTime timestamp;

}
