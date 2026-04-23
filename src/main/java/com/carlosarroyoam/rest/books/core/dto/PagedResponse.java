package com.carlosarroyoam.rest.books.core.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
public class PagedResponse<T> {
  private List<T> items;
  private PaginationResponse pagination;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface PagedResponseMapper {
    PagedResponseMapper INSTANCE = Mappers.getMapper(PagedResponseMapper.class);

    default <T> PagedResponse<T> toPagedResponse(Page<T> page) {
      PaginationResponse pagination =
          PaginationResponse.builder()
              .page(page.getNumber())
              .size(page.getSize())
              .totalItems(page.getTotalElements())
              .totalPages(page.getTotalPages())
              .build();

      return PagedResponse.<T>builder().items(page.getContent()).pagination(pagination).build();
    }
  }
}
