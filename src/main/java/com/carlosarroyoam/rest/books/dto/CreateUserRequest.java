package com.carlosarroyoam.rest.books.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
  @NotBlank
  @Size(min = 3, max = 128)
  private String name;

  @NotNull
  private Byte age;

  @NotBlank
  @Size(min = 3, max = 128)
  @Email
  private String email;

  @NotBlank
  @Size(min = 3, max = 128)
  private String username;

  @NotBlank
  @Size(min = 3, max = 128)
  private String password;

  @NotNull
  private Integer roleId;
}
