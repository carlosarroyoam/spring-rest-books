package com.carlosarroyoam.rest.books.shipment.dto;

import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Datos requeridos para actualizar el estado de un envío. */
@Getter
@Setter
@Builder
public class UpdateShipmentStatusRequest {
  @NotNull(message = "Status should not be null")
  private ShipmentStatus status;
}
