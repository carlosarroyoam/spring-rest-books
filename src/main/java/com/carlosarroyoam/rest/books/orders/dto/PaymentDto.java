package com.carlosarroyoam.rest.books.orders.dto;

import com.carlosarroyoam.rest.books.orders.entity.Payment;
import com.carlosarroyoam.rest.books.orders.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.orders.entity.PaymentStatus;
import java.math.BigDecimal;
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
public class PaymentDto {
  private Long id;
  private BigDecimal amount;
  private PaymentMethod method;
  private PaymentStatus status;
  private String transactionId;
  private Long orderId;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface PaymentDtoMapper {
    PaymentDtoMapper INSTANCE = Mappers.getMapper(PaymentDtoMapper.class);

    PaymentDto toDto(Payment entity);
  }
}
