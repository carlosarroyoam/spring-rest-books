package com.carlosarroyoam.rest.books.orders.dto;

import com.carlosarroyoam.rest.books.customer.dto.CustomerDto;
import com.carlosarroyoam.rest.books.orders.entity.Order;
import com.carlosarroyoam.rest.books.orders.entity.OrderStatus;
import com.carlosarroyoam.rest.books.payment.dto.PaymentDto;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentDto;
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
public class OrderDto {
  private Long id;
  private String orderNumber;
  private OrderStatus status;
  private List<OrderItemDto> items;
  private BigDecimal subtotal;
  private BigDecimal taxAmount;
  private BigDecimal shippingAmount;
  private BigDecimal total;
  private CustomerDto customer;
  private PaymentDto payment;
  private ShipmentDto shipment;
  private String notes;
  private String shippingAddress;
  private String billingAddress;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface OrderDtoMapper {
    OrderDtoMapper INSTANCE = Mappers.getMapper(OrderDtoMapper.class);

    OrderDto toDto(Order entity);

    List<OrderDto> toDtos(List<Order> entities);

    Order toEntity(CreateOrderRequestDto requestDto);
  }
}
