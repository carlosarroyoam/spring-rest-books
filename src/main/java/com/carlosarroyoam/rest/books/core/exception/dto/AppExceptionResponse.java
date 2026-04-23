package com.carlosarroyoam.rest.books.core.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@Builder
public class AppExceptionResponse {
  private String message;
  private String error;
  private Map<String, String> details;
  private int status;
  private String path;
  private ZonedDateTime timestamp;
}
