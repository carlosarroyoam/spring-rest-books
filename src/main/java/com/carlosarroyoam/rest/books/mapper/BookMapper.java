package com.carlosarroyoam.rest.books.mapper;

import com.carlosarroyoam.rest.books.dto.BookDto;
import com.carlosarroyoam.rest.books.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.dto.UpdateBookRequestDto;
import com.carlosarroyoam.rest.books.entity.Book;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = AuthorMapper.class)
public interface BookMapper {
  BookDto toDto(Book book);

  List<BookDto> toDtos(List<Book> books);

  Book createRequestToEntity(CreateBookRequestDto requestDto);

  Book updateRequestToEntity(UpdateBookRequestDto requestDto);
}
