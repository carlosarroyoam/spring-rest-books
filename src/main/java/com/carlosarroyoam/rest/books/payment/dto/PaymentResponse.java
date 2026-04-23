package com.carlosarroyoam.rest.books.payment.dto;

import com.carlosarroyoam.rest.books.payment.entity.Payment;
import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Getter
@Setter
@Builder
public class PaymentResponse {
  private Long id;
  private BigDecimal amount;
  private PaymentMethod method;
  private PaymentStatus status;
  private String transactionId;
  private Long orderId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface PaymentResponseMapper {
    PaymentResponseMapper INSTANCE = Mappers.getMapper(PaymentResponseMapper.class);

    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toDto(Payment entity);
  }
}
