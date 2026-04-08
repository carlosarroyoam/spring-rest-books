package com.carlosarroyoam.rest.books.author.dto;

import com.carlosarroyoam.rest.books.author.entity.AuthorStatus;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorSpecsDto {
  @Size(max = 128, message = "Name should be max 128")
  private String name;

  private AuthorStatus status;
}
