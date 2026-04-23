package com.carlosarroyoam.rest.books.core.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaginationResponse {
  private int page;
  private int size;
  private long totalItems;
  private long totalPages;
}
