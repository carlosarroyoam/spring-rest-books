package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.orders.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShipmentStatusRequestDto {
  @NotNull(message = "Status should not be null")
  private ShipmentStatus status;
}
