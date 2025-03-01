package com.carlosarroyoam.rest.books.mapper;

import com.carlosarroyoam.rest.books.dto.BookResponse;
import com.carlosarroyoam.rest.books.dto.CreateBookRequest;
import com.carlosarroyoam.rest.books.dto.UpdateBookRequest;
import com.carlosarroyoam.rest.books.entity.Book;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = AuthorMapper.class)
public interface BookMapper {
  BookResponse toDto(Book book);

  List<BookResponse> toDtos(List<Book> books);

  Book createRequestToEntity(CreateBookRequest createBookRequest);

  Book updateRequestToEntity(UpdateBookRequest updateBookRequest);
}
