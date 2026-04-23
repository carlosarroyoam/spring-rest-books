package com.carlosarroyoam.rest.books.author.dto;

import com.carlosarroyoam.rest.books.author.entity.AuthorStatus;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthorSpecs {
  @Size(max = 128, message = "Name should be max 128")
  private String name;

  private AuthorStatus status;
}
