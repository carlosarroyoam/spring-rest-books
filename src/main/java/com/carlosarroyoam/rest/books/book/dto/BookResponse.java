package com.carlosarroyoam.rest.books.book.dto;

import com.carlosarroyoam.rest.books.author.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.author.dto.AuthorResponse.AuthorResponseMapper;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.book.entity.BookStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Getter
@Setter
@Builder
public class BookResponse {
  private Long id;
  private String isbn;
  private String title;
  private String coverUrl;
  private BigDecimal price;
  private Boolean isAvailableOnline;
  private BookStatus status;
  private List<AuthorResponse> authors;
  private LocalDate publishedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {AuthorResponseMapper.class})
  public interface BookResponseMapper {
    BookResponseMapper INSTANCE = Mappers.getMapper(BookResponseMapper.class);

    BookResponse toDto(Book entity);

    List<BookResponse> toDtos(List<Book> entities);
  }
}
