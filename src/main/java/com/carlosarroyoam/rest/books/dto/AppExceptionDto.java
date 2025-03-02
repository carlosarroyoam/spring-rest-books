package com.carlosarroyoam.rest.books.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppExceptionDto {
  private String message;
  private String error;
  private Map<String, String> details;
  private int status;
  private String path;
  private ZonedDateTime timestamp;
}
