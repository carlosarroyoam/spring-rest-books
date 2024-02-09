package com.carlosarroyoam.rest.books.mappers;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.carlosarroyoam.rest.books.dtos.BookResponse;
import com.carlosarroyoam.rest.books.dtos.CreateBookRequest;
import com.carlosarroyoam.rest.books.dtos.UpdateBookRequest;
import com.carlosarroyoam.rest.books.entities.Book;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = AuthorMapper.class)
public interface BookMapper {

	@BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
	BookResponse toDto(Book book);

	@BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
	List<BookResponse> toDtos(List<Book> books);

	@BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
	Book createRequestToEntity(CreateBookRequest createBookRequest);

	@BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
	Book updateRequestToEntity(UpdateBookRequest updateBookRequest);

}
