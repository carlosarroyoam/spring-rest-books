package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos de {@link Shipment} mediante Spring Data JPA. */
public interface ShipmentRepository
    extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {
  /**
   * Busca el envío de una orden.
   *
   * @param orderId id de la orden
   * @return el envío de la orden, si existe
   */
  Optional<Shipment> findByOrderId(Long orderId);
}
