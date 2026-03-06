package com.carlosarroyoam.rest.books.core.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDto<T> {
  private List<T> items;
  private PaginationDto pagination;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface PagedResponseDtoMapper {
    PagedResponseDtoMapper INSTANCE = Mappers.getMapper(PagedResponseDtoMapper.class);

    default <T> PagedResponseDto<T> toPagedResponseDto(Page<T> page) {
      PaginationDto pagination = PaginationDto.builder()
          .page(page.getNumber())
          .size(page.getSize())
          .totalItems(page.getTotalElements())
          .totalPages(page.getTotalPages())
          .build();

      return PagedResponseDto.<T>builder().items(page.getContent()).pagination(pagination).build();
    }
  }
}
