package com.carlosarroyoam.bookservice.dtos;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {
	private String message;
	private String error;
	private int status;
	private String path;
	private ZonedDateTime timestamp;
}
