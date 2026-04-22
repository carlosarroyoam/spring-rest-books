package com.carlosarroyoam.rest.books.shipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.order.OrderRepository;
import com.carlosarroyoam.rest.books.order.entity.Order;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentResponse;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentSpecs;
import com.carlosarroyoam.rest.books.shipment.dto.UpdateShipmentStatusRequest;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {
  @Mock private ShipmentRepository shipmentRepository;

  @Mock private OrderRepository orderRepository;

  @InjectMocks private ShipmentService shipmentService;

  private Shipment shipment;
  private Order order;

  @BeforeEach
  void setUp() {
    order = Order.builder().id(1L).status(OrderStatus.CONFIRMED).build();

    shipment =
        Shipment.builder()
            .id(1L)
            .attentionName("Carlos Arroyo")
            .address("123 Main Street, Springfield")
            .status(ShipmentStatus.PENDING)
            .order(order)
            .build();
  }

  @Test
  @DisplayName("Given shipments exist, when find all, then returns paged shipments")
  void givenShipmentsExist_whenFindAll_thenReturnsPagedShipments() {
    Pageable pageable = PageRequest.of(0, 25);
    ShipmentSpecs shipmentSpecs = ShipmentSpecs.builder().build();
    List<Shipment> shipments = List.of(shipment);

    when(shipmentRepository.findAll(
            ArgumentMatchers.<Specification<Shipment>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(shipments, pageable, shipments.size()));

    PagedResponse<ShipmentResponse> response = shipmentService.findAll(shipmentSpecs, pageable);

    assertThat(response).isNotNull();
    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().get(0).getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Given shipment exists, when find by id, then returns shipment")
  void givenShipmentExists_whenFindById_thenReturnsShipment() {
    when(shipmentRepository.findById(anyLong())).thenReturn(Optional.of(shipment));

    ShipmentResponse shipmentDto = shipmentService.findById(1L);

    assertThat(shipmentDto).isNotNull();
    assertThat(shipmentDto.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Given shipment exists, when update status, then syncs order status")
  void givenShipmentExists_whenUpdateStatus_thenSyncsOrderStatus() {
    UpdateShipmentStatusRequest requestDto =
        UpdateShipmentStatusRequest.builder().status(ShipmentStatus.DELIVERED).build();

    when(shipmentRepository.findById(anyLong())).thenReturn(Optional.of(shipment));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    shipmentService.updateStatus(1L, requestDto);

    verify(shipmentRepository).save(any(Shipment.class));
    verify(orderRepository).save(any(Order.class));
    assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
  }

  @Test
  @DisplayName("Given shipment does not exist, when find by id, then throws not found")
  void givenShipmentDoesNotExist_whenFindById_thenThrowsNotFound() {
    when(shipmentRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> shipmentService.findById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
  }
}
