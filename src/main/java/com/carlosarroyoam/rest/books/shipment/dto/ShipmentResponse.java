package com.carlosarroyoam.rest.books.shipment.dto;

import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import java.time.LocalDateTime;
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
public class ShipmentResponse {
  private Long id;
  private String attentionName;
  private String address;
  private String phone;
  private ShipmentStatus status;
  private Long orderId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ShipmentResponseMapper {
    ShipmentResponseMapper INSTANCE = Mappers.getMapper(ShipmentResponseMapper.class);

    @Mapping(source = "order.id", target = "orderId")
    ShipmentResponse toDto(Shipment entity);
  }
}
