package com.carlosarroyoam.rest.books.order.dto;

import com.carlosarroyoam.rest.books.book.dto.BookResponse;
import com.carlosarroyoam.rest.books.book.dto.BookResponse.BookResponseMapper;
import com.carlosarroyoam.rest.books.order.entity.OrderItem;
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
public class OrderItemResponse {
  private Long id;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal totalPrice;
  private Long bookId;
  private BookResponse book;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      BookResponseMapper.class })
  public interface OrderItemResponseMapper {
    OrderItemResponseMapper INSTANCE = Mappers.getMapper(OrderItemResponseMapper.class);

    OrderItemResponse toDto(OrderItem entity);

    List<OrderItemResponse> toDtos(List<OrderItem> entities);
  }
}
