package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto.PagedResponseDtoMapper;
import com.carlosarroyoam.rest.books.core.specification.SpecificationBuilder;
import com.carlosarroyoam.rest.books.order.OrderRepository;
import com.carlosarroyoam.rest.books.order.entity.Order;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.carlosarroyoam.rest.books.order.entity.Order_;
import com.carlosarroyoam.rest.books.payment.dto.CreatePaymentRequestDto;
import com.carlosarroyoam.rest.books.payment.dto.PaymentDto;
import com.carlosarroyoam.rest.books.payment.dto.PaymentDto.PaymentDtoMapper;
import com.carlosarroyoam.rest.books.payment.dto.PaymentSpecsDto;
import com.carlosarroyoam.rest.books.payment.dto.UpdatePaymentStatusRequestDto;
import com.carlosarroyoam.rest.books.payment.entity.Payment;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import com.carlosarroyoam.rest.books.payment.entity.Payment_;
import com.carlosarroyoam.rest.books.shipment.ShipmentRepository;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
  public PagedResponseDto<PaymentDto> findAll(PaymentSpecsDto paymentSpecs, Pageable pageable) {
    Specification<Payment> spec = SpecificationBuilder.<Payment>builder()
        .equalsIfPresent(root -> root.get(Payment_.method), paymentSpecs.getMethod())
        .betweenIfPresent(root -> root.get(Payment_.amount), paymentSpecs.getMinAmount(),
            paymentSpecs.getMaxAmount())
        .equalsIfPresent(root -> root.get(Payment_.status), paymentSpecs.getStatus())
        .likeIfPresent(root -> root.get(Payment_.transactionId), paymentSpecs.getTransactionId())
        .equalsIfPresent(root -> root.get(Payment_.order).get(Order_.id), paymentSpecs.getOrderId())
        .build();

    Page<Payment> payments = paymentRepository.findAll(spec, pageable);

    return PagedResponseDtoMapper.INSTANCE
        .toPagedResponseDto(payments.map(PaymentDtoMapper.INSTANCE::toDto));
  }

  @Transactional
  public PaymentDto findById(Long paymentId) {
    Payment paymentById = findPaymentEntityById(paymentId);
    return PaymentDtoMapper.INSTANCE.toDto(paymentById);
  }

  @Transactional
  public PaymentDto create(CreatePaymentRequestDto requestDto) {
    Order orderById = findOrderEntityById(requestDto.getOrderId());

    if (Boolean.TRUE.equals(paymentRepository.existsByOrderId(orderById.getId()))) {
      log.warn(AppMessages.PAYMENT_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.PAYMENT_ALREADY_EXISTS_EXCEPTION);
    }

    Payment payment = Payment.builder()
        .amount(orderById.getTotal())
        .method(requestDto.getMethod())
        .status(PaymentStatus.COMPLETED)
        .transactionId(generateTransactionId())
        .order(orderById)
        .build();

    Payment savedPayment = paymentRepository.save(payment);

    orderById.setStatus(OrderStatus.CONFIRMED);
    orderRepository.save(orderById);

    createShipmentIfMissing(orderById);

    return PaymentDtoMapper.INSTANCE.toDto(savedPayment);
  }

  @Transactional
  public void updateStatus(Long paymentId, UpdatePaymentStatusRequestDto requestDto) {
    Payment paymentById = findPaymentEntityById(paymentId);
    paymentById.setStatus(requestDto.getStatus());
    paymentRepository.save(paymentById);

    Order orderById = findOrderEntityById(paymentById.getOrder().getId());
    orderById
        .setStatus(resolveOrderStatusFromPayment(requestDto.getStatus(), orderById.getStatus()));
    orderRepository.save(orderById);
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
        .order(order)
        .build();

    shipmentRepository.save(shipment);
  }

  private String generateTransactionId() {
    return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }
}
