package com.carlosarroyoam.rest.books.mapper;

import com.carlosarroyoam.rest.books.dto.AuthorResponseDto;
import com.carlosarroyoam.rest.books.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.entity.Author;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorMapper {
  AuthorResponseDto toDto(Author author);

  List<AuthorResponseDto> toDtos(List<Author> authors);

  Author toEntity(CreateAuthorRequestDto requestDto);
}
