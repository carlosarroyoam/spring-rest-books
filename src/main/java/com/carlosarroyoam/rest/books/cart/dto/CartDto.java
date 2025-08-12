package com.carlosarroyoam.rest.books.cart.dto;

import com.carlosarroyoam.rest.books.cart.entity.Cart;
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
public class CartDto {
  private Long id;
  private List<CartItemDto> items;
  private UserDto user;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ShoppingCartDtoMapper {
    ShoppingCartDtoMapper INSTANCE = Mappers.getMapper(ShoppingCartDtoMapper.class);

    CartDto toDto(Cart entity);
  }
}
