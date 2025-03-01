package com.carlosarroyoam.rest.books.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
  @NotBlank(message = "Current password should not be blank")
  private String currentPassword;

  @NotBlank(message = "New password  should not be blank")
  @Size(min = 3, max = 32, message = "New password  should be between 3 and 32")
  private String newPassword;

  @NotBlank(message = "Confirm password  should not be blank")
  @Size(min = 3, max = 32, message = "Confirm password  should be between 3 and 32")
  private String confirmPassword;
}
