package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentResponse;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentSpecs;
import com.carlosarroyoam.rest.books.shipment.dto.UpdateShipmentStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone las operaciones REST sobre {@link com.carlosarroyoam.rest.books.shipment.entity.Shipment}:
 * listado paginado con filtros, consulta por id y actualización de estado. Todas las operaciones
 * requieren rol {@code App/Admin}.
 */
@RestController
@RequestMapping("/shipments")
public class ShipmentController {
  private final ShipmentService shipmentService;

  public ShipmentController(ShipmentService shipmentService) {
    this.shipmentService = shipmentService;
  }

  /**
   * Lista envíos de forma paginada, aplicando los filtros de {@link ShipmentSpecs}.
   *
   * @param shipmentSpecs filtros opcionales de búsqueda
   * @param pageable configuración de página y orden
   * @return la página de envíos encontrados
   */
  @GetMapping(produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<PagedResponse<ShipmentResponse>> findAll(
      @Valid @ModelAttribute ShipmentSpecs shipmentSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<ShipmentResponse> shipments = shipmentService.findAll(shipmentSpecs, pageable);
    return ResponseEntity.ok(shipments);
  }

  /**
   * Busca un envío por su id.
   *
   * @param shipmentId id del envío a buscar
   * @return el envío encontrado
   */
  @GetMapping(value = "/{shipmentId}", produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<ShipmentResponse> findById(@PathVariable Long shipmentId) {
    ShipmentResponse shipmentById = shipmentService.findById(shipmentId);
    return ResponseEntity.ok(shipmentById);
  }

  /**
   * Actualiza el estado de un envío.
   *
   * @param shipmentId id del envío a actualizar
   * @param request nuevo estado del envío
   */
  @PutMapping(value = "/{shipmentId}/status", consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> updateStatus(
      @PathVariable Long shipmentId, @Valid @RequestBody UpdateShipmentStatusRequest request) {
    shipmentService.updateStatus(shipmentId, request);
    return ResponseEntity.noContent().build();
  }
}
