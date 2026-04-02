package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto.PagedResponseDtoMapper;
import com.carlosarroyoam.rest.books.orders.OrderRepository;
import com.carlosarroyoam.rest.books.orders.dto.ShipmentDto;
import com.carlosarroyoam.rest.books.orders.dto.ShipmentDto.ShipmentDtoMapper;
import com.carlosarroyoam.rest.books.orders.entity.Order;
import com.carlosarroyoam.rest.books.orders.entity.OrderStatus;
import com.carlosarroyoam.rest.books.orders.entity.Shipment;
import com.carlosarroyoam.rest.books.orders.entity.ShipmentStatus;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShipmentService {
  private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);
  private final ShipmentRepository shipmentRepository;
  private final OrderRepository orderRepository;

  public ShipmentService(ShipmentRepository shipmentRepository, OrderRepository orderRepository) {
    this.shipmentRepository = shipmentRepository;
    this.orderRepository = orderRepository;
  }

  @Transactional
  public PagedResponseDto<ShipmentDto> findAll(Pageable pageable) {
    Page<Shipment> shipments = shipmentRepository.findAll(pageable);
    return PagedResponseDtoMapper.INSTANCE
        .toPagedResponseDto(shipments.map(ShipmentDtoMapper.INSTANCE::toDto));
  }

  @Transactional
  public ShipmentDto findById(Long shipmentId) {
    Shipment shipment = findShipmentEntityById(shipmentId);
    return ShipmentDtoMapper.INSTANCE.toDto(shipment);
  }

  @Transactional
  public void updateStatus(Long shipmentId, UpdateShipmentStatusRequestDto requestDto) {
    Shipment shipment = findShipmentEntityById(shipmentId);
    shipment.setStatus(requestDto.getStatus());
    shipmentRepository.save(shipment);

    Order order = orderRepository.findById(shipment.getOrderId()).orElseThrow(() -> {
      log.warn(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.ORDER_NOT_FOUND_EXCEPTION);
    });
    order.setStatus(resolveOrderStatusFromShipment(requestDto.getStatus(), order.getStatus()));
    orderRepository.save(order);
  }

  private Shipment findShipmentEntityById(Long shipmentId) {
    return shipmentRepository.findById(shipmentId).orElseThrow(() -> {
      log.warn(AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
    });
  }

  private OrderStatus resolveOrderStatusFromShipment(ShipmentStatus shipmentStatus,
      OrderStatus currentStatus) {
    return switch (shipmentStatus) {
      case SHIPPED -> OrderStatus.SHIPPED;
      case DELIVERED -> OrderStatus.DELIVERED;
      case CANCELLED, RETURNED -> OrderStatus.CANCELLED;
      case PENDING -> currentStatus == null ? OrderStatus.PENDING : currentStatus;
    };
  }
}
