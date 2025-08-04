package com.carlosarroyoam.rest.books.author.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAuthorRequestDto {
  @NotBlank(message = "Name should not be blank")
  @Size(min = 3, max = 128, message = "Name should be between 3 and 128")
  private String name;
}
