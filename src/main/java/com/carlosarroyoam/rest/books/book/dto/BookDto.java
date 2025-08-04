package com.carlosarroyoam.rest.books.book.dto;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.AuthorDto.AuthorDtoMapper;
import com.carlosarroyoam.rest.books.book.entity.Book;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {
  private Long id;
  private String isbn;
  private String title;
  private String coverUrl;
  private List<AuthorDto> authors;
  private BigDecimal price;
  private Boolean isAvailableOnline;
  private LocalDate publishedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = AuthorDtoMapper.class)
  public interface BookDtoMapper {
    BookDtoMapper INSTANCE = Mappers.getMapper(BookDtoMapper.class);

    BookDto toDto(Book book);

    List<BookDto> toDtos(List<Book> books);

    Book createRequestToEntity(CreateBookRequestDto requestDto);
  }
}
