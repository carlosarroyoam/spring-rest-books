package com.carlosarroyoam.rest.books.shipment.dto;

import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
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
public class ShipmentDto {
  private Long id;
  private String attentionName;
  private String address;
  private String phone;
  private ShipmentStatus status;
  private Long orderId;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ShipmentDtoMapper {
    ShipmentDtoMapper INSTANCE = Mappers.getMapper(ShipmentDtoMapper.class);

    @Mapping(source = "order.id", target = "orderId")
    ShipmentDto toDto(Shipment entity);
  }
}
