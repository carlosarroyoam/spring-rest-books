package com.carlosarroyoam.rest.books.orders.dto;

import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.orders.entity.OrderItem;
import java.math.BigDecimal;
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
public class OrderItemDto {
  private Long id;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal totalPrice;
  private Long bookId;
  private BookDto book;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface OrderItemDtoMapper {
    OrderItemDtoMapper INSTANCE = Mappers.getMapper(OrderItemDtoMapper.class);

    OrderItemDto toDto(OrderItem entity);

    List<OrderItemDto> toDtos(List<OrderItem> entities);
  }
}
