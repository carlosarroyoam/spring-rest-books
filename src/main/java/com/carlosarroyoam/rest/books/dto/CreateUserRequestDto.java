package com.carlosarroyoam.rest.books.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class CreateUserRequestDto {
  @NotBlank(message = "Name should not be blank")
  @Size(min = 3, max = 128, message = "Name should be between 3 and 128")
  private String name;

  @NotNull(message = "Age should not be blank")
  @Min(value = 18, message = "Age should be min 18")
  @Max(value = 100, message = "Age should be max 100")
  private Byte age;

  @NotBlank(message = "Email should not be blank")
  @Email(message = "Email should be an valid email address")
  @Size(min = 3, max = 128, message = "Email should be between 3 and 128")
  private String email;

  @NotBlank(message = "Username should not be blank")
  @Size(min = 3, max = 128, message = "Email should be between 3 and 128")
  private String username;

  @NotNull(message = "Role_id should not be null")
  private Integer roleId;
}
