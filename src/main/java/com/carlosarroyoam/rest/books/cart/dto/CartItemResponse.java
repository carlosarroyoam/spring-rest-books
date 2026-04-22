package com.carlosarroyoam.rest.books.cart.dto;

import com.carlosarroyoam.rest.books.book.dto.BookResponse;
import com.carlosarroyoam.rest.books.book.dto.BookResponse.BookResponseMapper;
import com.carlosarroyoam.rest.books.cart.entity.CartItem;
import java.time.LocalDateTime;
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
public class CartItemResponse {
  private Long id;
  private Integer quantity;
  private BookResponse book;
  private LocalDateTime addedAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {BookResponseMapper.class})
  public interface CartItemResponseMapper {
    CartItemResponseMapper INSTANCE = Mappers.getMapper(CartItemResponseMapper.class);

    CartItemResponse toDto(CartItem entity);
  }
}
