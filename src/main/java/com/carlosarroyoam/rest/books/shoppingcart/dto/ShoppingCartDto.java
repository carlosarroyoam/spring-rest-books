package com.carlosarroyoam.rest.books.shoppingcart.dto;

import com.carlosarroyoam.rest.books.shoppingcart.entity.ShoppingCart;
import com.carlosarroyoam.rest.books.user.dto.UserDto;
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
public class ShoppingCartDto {
  private Long id;
  private List<CartItemDto> items;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ShoppingCartDtoMapper {
    ShoppingCartDtoMapper INSTANCE = Mappers.getMapper(ShoppingCartDtoMapper.class);

    ShoppingCartDto toDto(ShoppingCart entity);
  }
}
