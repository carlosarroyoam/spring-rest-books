package com.carlosarroyoam.rest.books.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserFilterDto {
  @Size(max = 128, message = "Name should be max 128")
  private String name;

  @Min(value = 18, message = "Age should be min 18")
  @Max(value = 100, message = "Age should be max 100")
  private Byte age;

  @Email(message = "Email should be an valid email address")
  @Size(max = 128, message = "Email should be max 128")
  private String email;

  @Size(max = 128, message = "Username should be max 128")
  private String username;

  private Boolean isActive;
  private Integer roleId;
}
