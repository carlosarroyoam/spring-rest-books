package com.carlosarroyoam.rest.books.order.dto;

import com.carlosarroyoam.rest.books.customer.dto.CustomerResponse;
import com.carlosarroyoam.rest.books.order.dto.OrderItemResponse.OrderItemResponseMapper;
import com.carlosarroyoam.rest.books.order.entity.Order;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.carlosarroyoam.rest.books.payment.dto.PaymentResponse;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentResponse;
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
public class OrderResponse {
  private Long id;
  private String orderNumber;
  private String notes;
  private String shippingAddress;
  private String billingAddress;
  private BigDecimal subtotal;
  private BigDecimal taxAmount;
  private BigDecimal shippingAmount;
  private BigDecimal total;
  private OrderStatus status;
  private List<OrderItemResponse> items;
  private CustomerResponse customer;
  private PaymentResponse payment;
  private ShipmentResponse shipment;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
      OrderItemResponseMapper.class, CustomerResponse.class, PaymentResponse.class,
      ShipmentResponse.class })
  public interface OrderResponseMapper {
    OrderResponseMapper INSTANCE = Mappers.getMapper(OrderResponseMapper.class);

    OrderResponse toDto(Order entity);

    List<OrderResponse> toDtos(List<Order> entities);
  }
}
