package com.carlosarroyoam.rest.books.book.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookFilterDto {
  @Size(min = 10, max = 17, message = "Isbn should be between 10 and 17")
  private String isbn;

  @Size(max = 128, message = "Title should be max 128")
  private String title;

  @Size(max = 128, message = "Author should be max 128")
  private String author;

  private Boolean isAvailableOnline;
}
