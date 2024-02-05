package com.carlosarroyoam.rest.books.exceptions;

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

import com.carlosarroyoam.rest.books.dtos.AppExceptionResponse;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ControllerAdvisor.class);

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<AppExceptionResponse> handleResponseStatusException(ResponseStatusException ex,
			WebRequest request) {
		AppExceptionResponse appExceptionResponse = getExceptionResponse(ex, ex.getStatusCode(), request);
		appExceptionResponse.setMessage(ex.getReason());

		return new ResponseEntity<>(appExceptionResponse, ex.getStatusCode());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<AppExceptionResponse> handleAllException(Exception ex, WebRequest request) {
		AppExceptionResponse appExceptionResponse = getExceptionResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
		log.error("Exception: {}", ex.getMessage());

		return ResponseEntity.internalServerError().body(appExceptionResponse);
	}

	private AppExceptionResponse getExceptionResponse(Exception ex, HttpStatusCode statusCode, WebRequest request) {
		AppExceptionResponse appExceptionResponse = new AppExceptionResponse();
		appExceptionResponse.setMessage(ex.getMessage());
		appExceptionResponse.setError(HttpStatus.valueOf(statusCode.value()).getReasonPhrase());
		appExceptionResponse.setStatus(statusCode.value());
		appExceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

		return appExceptionResponse;
	}

}