package com.carlosarroyoam.rest.books.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.carlosarroyoam.rest.books.dtos.BookResponse;
import com.carlosarroyoam.rest.books.dtos.CreateBookRequest;
import com.carlosarroyoam.rest.books.dtos.UpdateBookRequest;
import com.carlosarroyoam.rest.books.entities.Book;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = AuthorMapper.class)
public interface BookMapper {

	BookResponse toDto(Book book);

	List<BookResponse> toDtos(List<Book> books);

	Book createRequestToEntity(CreateBookRequest createBookRequest);

	Book updateRequestToEntity(UpdateBookRequest updateBookRequest);

}
