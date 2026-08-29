package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.pagination.PagedResponse;
import com.carlosarroyoam.rest.books.core.pagination.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.rest.books.core.specification.SpecificationBuilder;
import com.carlosarroyoam.rest.books.order.OrderRepository;
import com.carlosarroyoam.rest.books.order.entity.Order;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.carlosarroyoam.rest.books.order.entity.Order_;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentResponse;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentResponse.ShipmentResponseMapper;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentSpecs;
import com.carlosarroyoam.rest.books.shipment.dto.UpdateShipmentStatusRequest;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment_;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Contiene la lógica de negocio de {@link Shipment}: búsqueda paginada con filtros, consulta por id
 * y actualización de estado, reflejando el cambio en el estado de la {@link Order} asociada.
 */
@Service
public class ShipmentService {
  private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);
  private final ShipmentRepository shipmentRepository;
  private final OrderRepository orderRepository;

  public ShipmentService(ShipmentRepository shipmentRepository, OrderRepository orderRepository) {
    this.shipmentRepository = shipmentRepository;
    this.orderRepository = orderRepository;
  }

  /**
   * Busca envíos de forma paginada aplicando los filtros de {@link ShipmentSpecs}.
   *
   * @param shipmentSpecs filtros opcionales de búsqueda
   * @param pageable configuración de página y orden
   * @return la página de envíos encontrados
   */
  @Transactional(readOnly = true)
  public PagedResponse<ShipmentResponse> findAll(ShipmentSpecs shipmentSpecs, Pageable pageable) {
    Specification<Shipment> spec =
        SpecificationBuilder.<Shipment>builder()
            .likeIfPresent(
                root -> root.get(Shipment_.attentionName), shipmentSpecs.getAttentionName())
            .likeIfPresent(root -> root.get(Shipment_.address), shipmentSpecs.getAddress())
            .likeIfPresent(root -> root.get(Shipment_.phone), shipmentSpecs.getPhone())
            .equalsIfPresent(root -> root.get(Shipment_.status), shipmentSpecs.getStatus())
            .betweenDatesIfPresent(
                root -> root.get(Shipment_.createdAt),
                shipmentSpecs.getStartDate(),
                shipmentSpecs.getEndDate())
            .equalsIfPresent(
                root -> root.join(Shipment_.order).get(Order_.id), shipmentSpecs.getOrderId())
            .build();

    Page<Shipment> shipments = shipmentRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        shipments.map(ShipmentResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un envío por su id.
   *
   * @param shipmentId id del envío a buscar
   * @return el envío encontrado
   */
  @Transactional(readOnly = true)
  public ShipmentResponse findById(Long shipmentId) {
    Shipment shipmentById = findShipmentByIdOrFail(shipmentId);
    return ShipmentResponseMapper.INSTANCE.toDto(shipmentById);
  }

  /**
   * Actualiza el estado de un envío y refleja el cambio en el estado de su orden asociada.
   *
   * @param shipmentId id del envío a actualizar
   * @param request nuevo estado del envío
   */
  @Transactional
  public void updateStatus(Long shipmentId, UpdateShipmentStatusRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Shipment shipmentById = findShipmentByIdOrFail(shipmentId);
    shipmentById.setStatus(request.getStatus());
    shipmentById.setUpdatedAt(now);
    shipmentRepository.save(shipmentById);

    Order orderById = findOrderByIdOrFail(shipmentById.getOrder().getId());
    orderById.setStatus(resolveOrderStatusFromShipment(request.getStatus(), orderById.getStatus()));
    orderById.setUpdatedAt(now);
    orderRepository.save(orderById);
  }

  /**
   * Busca un envío por id o lanza {@code 404 Not Found} si no existe.
   *
   * @param shipmentId id del envío a buscar
   * @return el envío encontrado
   */
  private Shipment findShipmentByIdOrFail(Long shipmentId) {
    return shipmentRepository
        .findById(shipmentId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
            });
  }

  /**
   * Resuelve el estado que debe tomar una orden a partir del nuevo estado de su envío.
   *
   * @param shipmentStatus nuevo estado del envío
   * @param currentStatus estado actual de la orden, usado cuando el envío queda {@code PENDING}
   * @return el estado que debe tomar la orden
   */
  private OrderStatus resolveOrderStatusFromShipment(
      ShipmentStatus shipmentStatus, OrderStatus currentStatus) {
    return switch (shipmentStatus) {
      case SHIPPED -> OrderStatus.SHIPPED;
      case DELIVERED -> OrderStatus.DELIVERED;
      case CANCELLED, RETURNED -> OrderStatus.CANCELLED;
      case PENDING -> currentStatus == null ? OrderStatus.PENDING : currentStatus;
    };
  }

  /**
   * Busca una orden por id o lanza {@code 404 Not Found} si no existe.
   *
   * @param orderId id de la orden a buscar
   * @return la orden encontrada
   */
  private Order findOrderByIdOrFail(Long orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.ORDER_NOT_FOUND_EXCEPTION);
            });
  }
}
