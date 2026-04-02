package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.orders.dto.ShipmentDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {
  private final ShipmentService shipmentService;

  public ShipmentController(ShipmentService shipmentService) {
    this.shipmentService = shipmentService;
  }

  @GetMapping(produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<PagedResponseDto<ShipmentDto>> findAll(
      @PageableDefault(page = 0, size = 25) Pageable pageable) {
    PagedResponseDto<ShipmentDto> shipments = shipmentService.findAll(pageable);
    return ResponseEntity.ok(shipments);
  }

  @GetMapping(value = "/{shipmentId}", produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<ShipmentDto> findById(@PathVariable Long shipmentId) {
    ShipmentDto shipment = shipmentService.findById(shipmentId);
    return ResponseEntity.ok(shipment);
  }

  @PutMapping(value = "/{shipmentId}/status", consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> updateStatus(@PathVariable Long shipmentId,
      @Valid @RequestBody UpdateShipmentStatusRequestDto requestDto) {
    shipmentService.updateStatus(shipmentId, requestDto);
    return ResponseEntity.noContent().build();
  }
}
