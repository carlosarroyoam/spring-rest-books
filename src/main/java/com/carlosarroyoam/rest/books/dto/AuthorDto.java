package com.carlosarroyoam.rest.books.dto;

import com.carlosarroyoam.rest.books.entity.Author;
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
public class AuthorDto {
  private Long id;
  private String name;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface AuthorDtoMapper {
    AuthorDtoMapper INSTANCE = Mappers.getMapper(AuthorDtoMapper.class);

    AuthorDto toDto(Author author);

    List<AuthorDto> toDtos(List<Author> authors);

    Author toEntity(CreateAuthorRequestDto requestDto);
  }
}
