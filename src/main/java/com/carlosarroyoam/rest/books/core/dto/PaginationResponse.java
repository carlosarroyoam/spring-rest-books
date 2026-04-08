package com.carlosarroyoam.rest.books.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationResponse {
  private int page;
  private int size;
  private long totalItems;
  private long totalPages;
}
