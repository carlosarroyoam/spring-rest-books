package com.carlosarroyoam.rest.books.author.dto;

import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.author.entity.AuthorStatus;
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
public class AuthorResponse {
  private Long id;
  private String name;
  private String bio;
  private AuthorStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface AuthorResponseMapper {
    AuthorResponseMapper INSTANCE = Mappers.getMapper(AuthorResponseMapper.class);

    AuthorResponse toDto(Author entity);

    List<AuthorResponse> toDtos(List<Author> entities);
  }
}
