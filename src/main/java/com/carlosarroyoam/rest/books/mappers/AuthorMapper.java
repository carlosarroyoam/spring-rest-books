package com.carlosarroyoam.rest.books.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.carlosarroyoam.rest.books.dtos.AuthorResponse;
import com.carlosarroyoam.rest.books.entities.Author;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorMapper {

	AuthorResponse toDto(Author author);

	List<AuthorResponse> toDtos(List<Author> authors);

}
