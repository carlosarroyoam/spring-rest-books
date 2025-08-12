package com.carlosarroyoam.rest.books.shoppingcart.dto;

import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.shoppingcart.entity.CartItem;
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
public class CartItemDto {
  private Long id;
  private BookDto book;
  private Integer quantity;
  private LocalDateTime addedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface CartItemDtoMapper {
    CartItemDtoMapper INSTANCE = Mappers.getMapper(CartItemDtoMapper.class);

    CartItemDto toDto(CartItem entity);

    CartItem updateCartItemToEntity(UpdateCartItemRequestDto requestDto);
  }
}
