package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.orders.OrderRepository;
import com.carlosarroyoam.rest.books.orders.entity.Order;
import com.carlosarroyoam.rest.books.orders.entity.OrderStatus;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentDto;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentSpecsDto;
import com.carlosarroyoam.rest.books.shipment.dto.UpdateShipmentStatusRequestDto;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {
  @Mock
  private ShipmentRepository shipmentRepository;

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private ShipmentService shipmentService;

  private Shipment shipment;
  private Order order;

  @BeforeEach
  void setUp() {
    order = Order.builder().id(1L).status(OrderStatus.CONFIRMED).build();

    shipment = Shipment.builder()
        .id(1L)
        .attentionName("Carlos Arroyo")
        .address("123 Main Street, Springfield")
        .status(ShipmentStatus.PENDING)
        .order(order)
        .build();
  }

  @Test
  @DisplayName("Should return PagedResponseDto<ShipmentDto> when find all shipments")
  void shouldReturnListOfShipments() {
    Pageable pageable = PageRequest.of(0, 25);
    ShipmentSpecsDto shipmentSpecs = ShipmentSpecsDto.builder().build();
    List<Shipment> shipments = List.of(shipment);

    when(shipmentRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(shipments, pageable, shipments.size()));

    PagedResponseDto<ShipmentDto> response = shipmentService.findAll(pageable, shipmentSpecs);

    assertThat(response).isNotNull();
    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().get(0).getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should return ShipmentDto when find shipment by id with existing id")
  void shouldReturnWhenFindShipmentByIdWithExistingId() {
    when(shipmentRepository.findById(anyLong())).thenReturn(Optional.of(shipment));

    ShipmentDto shipmentDto = shipmentService.findById(1L);

    assertThat(shipmentDto).isNotNull();
    assertThat(shipmentDto.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should update shipment status and sync order status")
  void shouldUpdateShipmentStatusAndSyncOrderStatus() {
    UpdateShipmentStatusRequestDto requestDto = UpdateShipmentStatusRequestDto.builder()
        .status(ShipmentStatus.DELIVERED)
        .build();

    when(shipmentRepository.findById(anyLong())).thenReturn(Optional.of(shipment));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    shipmentService.updateStatus(1L, requestDto);

    verify(shipmentRepository).save(any(Shipment.class));
    verify(orderRepository).save(any(Order.class));
    assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find shipment by id with non existing id")
  void shouldThrowWhenFindShipmentByIdWithNonExistingId() {
    when(shipmentRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> shipmentService.findById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.SHIPMENT_NOT_FOUND_EXCEPTION);
  }
}
