package com.carlosarroyoam.rest.books.dto;

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
public class UpdateAuthorRequest {
  @NotBlank
  @Size(min = 3, max = 128)
  private String name;
}
