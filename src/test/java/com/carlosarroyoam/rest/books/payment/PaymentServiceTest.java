package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.order.OrderRepository;
import com.carlosarroyoam.rest.books.order.entity.Order;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.carlosarroyoam.rest.books.payment.dto.CreatePaymentRequestDto;
import com.carlosarroyoam.rest.books.payment.dto.PaymentDto;
import com.carlosarroyoam.rest.books.payment.dto.PaymentSpecsDto;
import com.carlosarroyoam.rest.books.payment.dto.UpdatePaymentStatusRequestDto;
import com.carlosarroyoam.rest.books.payment.entity.Payment;
import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import com.carlosarroyoam.rest.books.shipment.ShipmentRepository;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ShipmentRepository shipmentRepository;

  @InjectMocks
  private PaymentService paymentService;

  private Payment payment;
  private Order order;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    order = Order.builder()
        .id(1L)
        .orderNumber("ORD-12345678")
        .status(OrderStatus.PENDING)
        .total(new BigDecimal("53.34"))
        .shippingAddress("123 Main Street, Springfield")
        .createdAt(now)
        .updatedAt(now)
        .build();

    payment = Payment.builder()
        .id(1L)
        .amount(new BigDecimal("53.34"))
        .method(PaymentMethod.CREDIT_CARD)
        .status(PaymentStatus.COMPLETED)
        .transactionId("PAY-ABC123")
        .order(order)
        .build();
  }

  @Test
  @DisplayName("Should return PagedResponseDto<PaymentDto> when find all payments")
  void shouldReturnListOfPayments() {
    Pageable pageable = PageRequest.of(0, 25);
    PaymentSpecsDto paymentSpecs = PaymentSpecsDto.builder().build();
    List<Payment> payments = List.of(payment);

    when(paymentRepository.findAll(ArgumentMatchers.<Specification<Payment>>any(),
        any(Pageable.class))).thenReturn(new PageImpl<>(payments, pageable, payments.size()));

    PagedResponseDto<PaymentDto> response = paymentService.findAll(pageable, paymentSpecs);

    assertThat(response).isNotNull();
    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().get(0).getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should return PaymentDto when find payment by id with existing id")
  void shouldReturnWhenFindPaymentByIdWithExistingId() {
    when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(payment));

    PaymentDto paymentDto = paymentService.findById(1L);

    assertThat(paymentDto).isNotNull();
    assertThat(paymentDto.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find payment by id with non existing id")
  void shouldThrowWhenFindPaymentByIdWithNonExistingId() {
    when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.findById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.PAYMENT_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return PaymentDto when create payment with valid data and create shipment if missing")
  void shouldReturnWhenCreatePaymentWithValidData() {
    CreatePaymentRequestDto requestDto = CreatePaymentRequestDto.builder()
        .orderId(1L)
        .method(PaymentMethod.CREDIT_CARD)
        .build();

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(paymentRepository.existsByOrderId(1L)).thenReturn(false);
    when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
      Payment savedPayment = invocation.getArgument(0);
      savedPayment.setId(1L);
      return savedPayment;
    });
    when(shipmentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

    PaymentDto paymentDto = paymentService.create(requestDto);

    assertThat(paymentDto).isNotNull();
    assertThat(paymentDto.getId()).isEqualTo(1L);
    assertThat(paymentDto.getAmount()).isEqualByComparingTo("53.34");
    assertThat(paymentDto.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(paymentDto.getOrderId()).isEqualTo(1L);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    verify(shipmentRepository).save(any(Shipment.class));
  }

  @Test
  @DisplayName("Should update payment status and sync order status")
  void shouldUpdatePaymentStatusAndSyncOrderStatus() {
    UpdatePaymentStatusRequestDto requestDto = UpdatePaymentStatusRequestDto.builder()
        .status(PaymentStatus.REFUNDED)
        .build();

    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    paymentService.updateStatus(1L, requestDto);

    verify(paymentRepository).save(any(Payment.class));
    verify(orderRepository).save(any(Order.class));
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
  }

  @Test
  @DisplayName("Should not create shipment when one already exists for order")
  void shouldNotCreateShipmentWhenOneAlreadyExists() {
    CreatePaymentRequestDto requestDto = CreatePaymentRequestDto.builder()
        .orderId(1L)
        .method(PaymentMethod.CREDIT_CARD)
        .build();

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(paymentRepository.existsByOrderId(1L)).thenReturn(false);
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
    when(shipmentRepository.findByOrderId(1L)).thenReturn(Optional.of(Shipment.builder()
        .id(1L)
        .order(Order.builder().build())
        .status(ShipmentStatus.PENDING)
        .build()));

    paymentService.create(requestDto);

    verify(shipmentRepository).findByOrderId(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create payment with non existing order")
  void shouldThrowWhenCreatePaymentWithNonExistingOrder() {
    CreatePaymentRequestDto requestDto = CreatePaymentRequestDto.builder()
        .orderId(99L)
        .method(PaymentMethod.CREDIT_CARD)
        .build();

    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create payment for order already paid")
  void shouldThrowWhenCreatePaymentForOrderAlreadyPaid() {
    CreatePaymentRequestDto requestDto = CreatePaymentRequestDto.builder()
        .orderId(1L)
        .method(PaymentMethod.CREDIT_CARD)
        .build();

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(paymentRepository.existsByOrderId(1L)).thenReturn(true);

    assertThatThrownBy(() -> paymentService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.PAYMENT_ALREADY_EXISTS_EXCEPTION);
  }
}
