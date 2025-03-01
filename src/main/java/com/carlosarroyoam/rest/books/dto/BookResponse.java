package com.carlosarroyoam.rest.books.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
  private Long id;
  private String isbn;
  private String title;
  private String coverUrl;
  private List<AuthorResponse> authors;
  private Double price;
  private Boolean isAvailableOnline;
  private LocalDate publishedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
