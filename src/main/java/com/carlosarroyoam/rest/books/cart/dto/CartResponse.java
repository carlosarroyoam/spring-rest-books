package com.carlosarroyoam.rest.books.cart.dto;

import com.carlosarroyoam.rest.books.cart.dto.CartItemResponse.CartItemResponseMapper;
import com.carlosarroyoam.rest.books.cart.entity.Cart;
import com.carlosarroyoam.rest.books.customer.dto.CustomerResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Getter
@Setter
@Builder
public class CartResponse {
  private Long id;
  private List<CartItemResponse> items;
  private CustomerResponse customer;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE,
      uses = {CartItemResponseMapper.class})
  public interface CartResponseMapper {
    CartResponseMapper INSTANCE = Mappers.getMapper(CartResponseMapper.class);

    CartResponse toDto(Cart entity);
  }
}
