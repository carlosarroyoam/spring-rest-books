package com.carlosarroyoam.rest.books.payment.dto;

import com.carlosarroyoam.rest.books.payment.entity.Payment;
import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    @Mapping(source = "order.id", target = "orderId")
    PaymentDto toDto(Payment entity);
  }
}
