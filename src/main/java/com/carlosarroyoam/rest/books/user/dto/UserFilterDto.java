package com.carlosarroyoam.rest.books.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserFilterDto {
  @Size(min = 3, max = 128, message = "First name should be between 3 and 128")
  private String firstName;

  @Size(min = 3, max = 128, message = "Last name should be between 3 and 128")
  private String lastName;

  @Email(message = "Email should be an valid email address")
  @Size(max = 128, message = "Email should be max 128")
  private String email;

  @Size(max = 128, message = "Username should be max 128")
  private String username;
}
