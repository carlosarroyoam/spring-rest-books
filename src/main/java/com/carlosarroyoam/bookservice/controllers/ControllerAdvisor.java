package com.carlosarroyoam.bookservice.controllers;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.carlosarroyoam.bookservice.dtos.ExceptionResponse;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ControllerAdvisor.class);

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Object> handleSQLException(WebRequest request, ResponseStatusException ex) {
		ExceptionResponse exceptionResponse = getExceptionResponse(ex, ex.getStatusCode(), request);
		exceptionResponse.setMessage(ex.getReason());

		return new ResponseEntity<>(exceptionResponse, ex.getStatusCode());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponse> exception(Exception ex, WebRequest request) {
		ExceptionResponse exceptionResponse = getExceptionResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
		log.error("Exception: {}", ex.getMessage());

		return ResponseEntity.internalServerError().body(exceptionResponse);
	}

	private ExceptionResponse getExceptionResponse(Exception ex, HttpStatusCode statusCode, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		exceptionResponse.setMessage(ex.getMessage());
		exceptionResponse.setError(HttpStatus.valueOf(statusCode.value()).getReasonPhrase());
		exceptionResponse.setStatus(statusCode.value());
		exceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		exceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

		return exceptionResponse;
	}

}