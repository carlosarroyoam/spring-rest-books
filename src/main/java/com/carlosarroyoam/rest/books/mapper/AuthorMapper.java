package com.carlosarroyoam.rest.books.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.carlosarroyoam.rest.books.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.dto.CreateAuthorRequest;
import com.carlosarroyoam.rest.books.entity.Author;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorMapper {

	AuthorResponse toDto(Author author);

	List<AuthorResponse> toDtos(List<Author> authors);

	Author toEntity(CreateAuthorRequest createAuthorRequest);

}
