package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto.PagedResponseDtoMapper;
import com.carlosarroyoam.rest.books.orders.OrderRepository;
import com.carlosarroyoam.rest.books.orders.dto.PaymentDto;
import com.carlosarroyoam.rest.books.orders.dto.PaymentDto.PaymentDtoMapper;
import com.carlosarroyoam.rest.books.orders.entity.Order;
import com.carlosarroyoam.rest.books.orders.entity.OrderStatus;
import com.carlosarroyoam.rest.books.orders.entity.Payment;
import com.carlosarroyoam.rest.books.orders.entity.PaymentStatus;
import com.carlosarroyoam.rest.books.orders.entity.Shipment;
import com.carlosarroyoam.rest.books.orders.entity.ShipmentStatus;
import com.carlosarroyoam.rest.books.payment.dto.CreatePaymentRequestDto;
import com.carlosarroyoam.rest.books.payment.dto.UpdatePaymentStatusRequestDto;
import com.carlosarroyoam.rest.books.shipment.ShipmentRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {
  private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final ShipmentRepository shipmentRepository;

  public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository,
      ShipmentRepository shipmentRepository) {
    this.paymentRepository = paymentRepository;
    this.orderRepository = orderRepository;
    this.shipmentRepository = shipmentRepository;
  }

  @Transactional
  public PagedResponseDto<PaymentDto> findAll(Pageable pageable) {
    Page<Payment> payments = paymentRepository.findAll(pageable);
    return PagedResponseDtoMapper.INSTANCE
        .toPagedResponseDto(payments.map(PaymentDtoMapper.INSTANCE::toDto));
  }

  @Transactional
  public PaymentDto findById(Long paymentId) {
    Payment payment = findPaymentEntityById(paymentId);
    return PaymentDtoMapper.INSTANCE.toDto(payment);
  }

  @Transactional
  public PaymentDto create(CreatePaymentRequestDto requestDto) {
    Order order = findOrderEntityById(requestDto.getOrderId());

    if (Boolean.TRUE.equals(paymentRepository.existsByOrderId(order.getId()))) {
      log.warn(AppMessages.PAYMENT_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.PAYMENT_ALREADY_EXISTS_EXCEPTION);
    }

    Payment payment = Payment.builder()
        .amount(order.getTotal())
        .method(requestDto.getMethod())
        .status(PaymentStatus.COMPLETED)
        .transactionId(generateTransactionId())
        .orderId(order.getId())
        .order(order)
        .build();

    Payment savedPayment = paymentRepository.save(payment);

    order.setStatus(OrderStatus.CONFIRMED);
    orderRepository.save(order);

    createShipmentIfMissing(order);

    return PaymentDtoMapper.INSTANCE.toDto(savedPayment);
  }

  @Transactional
  public void updateStatus(Long paymentId, UpdatePaymentStatusRequestDto requestDto) {
    Payment payment = findPaymentEntityById(paymentId);
    payment.setStatus(requestDto.getStatus());
    paymentRepository.save(payment);

    Order order = findOrderEntityById(payment.getOrderId());
    order.setStatus(resolveOrderStatusFromPayment(requestDto.getStatus(), order.getStatus()));
    orderRepository.save(order);
  }

  private Payment findPaymentEntityById(Long paymentId) {
    return paymentRepository.findById(paymentId).orElseThrow(() -> {
      log.warn(AppMessages.PAYMENT_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.PAYMENT_NOT_FOUND_EXCEPTION);
    });
  }

  private Order findOrderEntityById(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> {
      log.warn(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.ORDER_NOT_FOUND_EXCEPTION);
    });
  }

  private OrderStatus resolveOrderStatusFromPayment(PaymentStatus paymentStatus,
      OrderStatus currentStatus) {
    return switch (paymentStatus) {
    case COMPLETED -> OrderStatus.CONFIRMED;
    case FAILED, CANCELLED -> OrderStatus.CANCELLED;
    case REFUNDED -> OrderStatus.REFUNDED;
    case PENDING -> currentStatus == null ? OrderStatus.PENDING : currentStatus;
    };
  }

  private void createShipmentIfMissing(Order order) {
    if (shipmentRepository.findByOrderId(order.getId()).isPresent()) {
      return;
    }

    String attentionName = order.getCustomer() == null ? null
        : order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();

    Shipment shipment = Shipment.builder()
        .attentionName(attentionName)
        .address(order.getShippingAddress())
        .status(ShipmentStatus.PENDING)
        .orderId(order.getId())
        .order(order)
        .build();

    shipmentRepository.save(shipment);
  }

  private String generateTransactionId() {
    return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }
}
