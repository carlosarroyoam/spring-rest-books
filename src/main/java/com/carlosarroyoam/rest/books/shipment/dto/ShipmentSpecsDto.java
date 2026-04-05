package com.carlosarroyoam.rest.books.shipment.dto;

import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShipmentSpecsDto {
  @Size(max = 128, message = "Attention name should be max 128")
  private String attentionName;

  @Size(max = 512, message = "Address should be max 512")
  private String address;

  @Size(max = 12, message = "Phone should be max 12")
  private String phone;

  private ShipmentStatus status;
  private Long orderId;
}