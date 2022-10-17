package com.carlosarroyoam.bookservice.controllers;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.carlosarroyoam.bookservice.dtos.ExceptionResponse;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ExceptionResponse> exception(RuntimeException ex, WebRequest request) {
		ExceptionResponse exceptionResponse = getExceptionResponse(ex, request);

		return ResponseEntity.internalServerError().body(exceptionResponse);
	}

	private ExceptionResponse getExceptionResponse(Throwable ex, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		exceptionResponse.setMessage(ex.getMessage());
		exceptionResponse.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		exceptionResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		exceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		exceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")).withFixedOffsetZone());

		return exceptionResponse;
	}
}