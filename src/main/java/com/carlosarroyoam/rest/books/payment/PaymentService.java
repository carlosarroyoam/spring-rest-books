package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.rest.books.core.specification.SpecificationBuilder;
import com.carlosarroyoam.rest.books.order.OrderRepository;
import com.carlosarroyoam.rest.books.order.entity.Order;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.carlosarroyoam.rest.books.order.entity.Order_;
import com.carlosarroyoam.rest.books.payment.dto.CreatePaymentRequest;
import com.carlosarroyoam.rest.books.payment.dto.PaymentResponse;
import com.carlosarroyoam.rest.books.payment.dto.PaymentResponse.PaymentResponseMapper;
import com.carlosarroyoam.rest.books.payment.dto.PaymentSpecs;
import com.carlosarroyoam.rest.books.payment.dto.UpdatePaymentStatusRequest;
import com.carlosarroyoam.rest.books.payment.entity.Payment;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import com.carlosarroyoam.rest.books.payment.entity.Payment_;
import com.carlosarroyoam.rest.books.shipment.ShipmentRepository;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  @Transactional(readOnly = true)
  public PagedResponse<PaymentResponse> findAll(PaymentSpecs paymentSpecs, Pageable pageable) {
    Specification<Payment> spec = SpecificationBuilder.<Payment>builder()
        .equalsIfPresent(root -> root.get(Payment_.method), paymentSpecs.getMethod())
        .betweenIfPresent(root -> root.get(Payment_.amount), paymentSpecs.getMinAmount(),
            paymentSpecs.getMaxAmount())
        .equalsIfPresent(root -> root.get(Payment_.status), paymentSpecs.getStatus())
        .betweenDatesIfPresent(root -> root.get(Payment_.createdAt), paymentSpecs.getStartDate(),
            paymentSpecs.getEndDate())
        .likeIfPresent(root -> root.get(Payment_.transactionId), paymentSpecs.getTransactionId())
        .equalsIfPresent(root -> root.join(Payment_.order).get(Order_.id),
            paymentSpecs.getOrderId())
        .build();

    Page<Payment> payments = paymentRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(payments.map(PaymentResponseMapper.INSTANCE::toDto));
  }

  @Transactional(readOnly = true)
  public PaymentResponse findById(Long paymentId) {
    Payment paymentById = findPaymentByIdOrFail(paymentId);
    return PaymentResponseMapper.INSTANCE.toDto(paymentById);
  }

  @Transactional
  public PaymentResponse create(CreatePaymentRequest request) {
    Order orderById = findOrderByIdOrFail(request.getOrderId());

    if (paymentRepository.existsByOrderId(orderById.getId())) {
      log.warn(AppMessages.PAYMENT_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.PAYMENT_ALREADY_EXISTS_EXCEPTION);
    }

    LocalDateTime now = LocalDateTime.now();
    Payment payment = Payment.builder()
        .amount(orderById.getTotal())
        .method(request.getMethod())
        .status(PaymentStatus.COMPLETED)
        .transactionId(generateTransactionId())
        .order(orderById)
        .createdAt(now)
        .updatedAt(now)
        .build();

    Payment savedPayment = paymentRepository.save(payment);

    orderById.setStatus(OrderStatus.CONFIRMED);
    orderById.setUpdatedAt(now);
    orderRepository.save(orderById);

    createShipmentIfMissing(orderById);

    return PaymentResponseMapper.INSTANCE.toDto(savedPayment);
  }

  @Transactional
  public void updateStatus(Long paymentId, UpdatePaymentStatusRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Payment paymentById = findPaymentByIdOrFail(paymentId);
    paymentById.setStatus(request.getStatus());
    paymentById.setUpdatedAt(now);
    paymentRepository.save(paymentById);

    Order orderById = findOrderByIdOrFail(paymentById.getOrder().getId());
    orderById.setStatus(resolveOrderStatusFromPayment(request.getStatus(), orderById.getStatus()));
    orderById.setUpdatedAt(now);
    orderRepository.save(orderById);
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

    LocalDateTime now = LocalDateTime.now();
    Shipment shipment = Shipment.builder()
        .attentionName(attentionName)
        .address(order.getShippingAddress())
        .status(ShipmentStatus.PENDING)
        .order(order)
        .createdAt(now)
        .updatedAt(now)
        .build();

    shipmentRepository.save(shipment);
  }

  private String generateTransactionId() {
    return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }

  private Payment findPaymentByIdOrFail(Long paymentId) {
    return paymentRepository.findById(paymentId).orElseThrow(() -> {
      log.warn(AppMessages.PAYMENT_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.PAYMENT_NOT_FOUND_EXCEPTION);
    });
  }

  private Order findOrderByIdOrFail(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> {
      log.warn(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.ORDER_NOT_FOUND_EXCEPTION);
    });
  }
}
