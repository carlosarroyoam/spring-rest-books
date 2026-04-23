package com.carlosarroyoam.rest.books.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdateCustomerRequest {
  @NotBlank(message = "First name should not be blank")
  @Size(min = 3, max = 128, message = "First name should be between 3 and 128")
  private String firstName;

  @NotBlank(message = "Last name should not be blank")
  @Size(min = 3, max = 128, message = "Last name should be between 3 and 128")
  private String lastName;
}
