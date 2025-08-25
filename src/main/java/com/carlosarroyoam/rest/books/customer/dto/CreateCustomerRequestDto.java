package com.carlosarroyoam.rest.books.customer.dto;

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
public class CreateCustomerRequestDto {
  @NotBlank(message = "First name should not be blank")
  @Size(min = 3, max = 128, message = "First name should be between 3 and 128")
  private String firstName;

  @NotBlank(message = "Last name should not be blank")
  @Size(min = 3, max = 128, message = "Last name should be between 3 and 128")
  private String lastName;

  @NotNull(message = "Password should not be null")
  @Size(min = 10, max = 35, message = "Password should be between 10 and 35")
  private String password;

  @NotBlank(message = "Email should not be blank")
  @Email(message = "Email should be an valid email address")
  @Size(min = 3, max = 128, message = "Email should be between 3 and 128")
  private String email;

  @NotBlank(message = "Username should not be blank")
  @Size(min = 3, max = 128, message = "Username should be between 3 and 128")
  private String username;
}
