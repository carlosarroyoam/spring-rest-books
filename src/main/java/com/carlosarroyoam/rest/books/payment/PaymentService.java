package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.exception.ResourceAlreadyExistsException;
import com.carlosarroyoam.rest.books.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.rest.books.core.pagination.PagedResponse;
import com.carlosarroyoam.rest.books.core.pagination.PagedResponse.PagedResponseMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contiene la lógica de negocio de {@link Payment}: búsqueda paginada con filtros, registro de un
 * pago (con actualización del estado de la {@link Order} y creación del {@link Shipment} asociado
 * si aún no existe) y actualización de estado.
 */
@Service
public class PaymentService {
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final ShipmentRepository shipmentRepository;

  public PaymentService(
      PaymentRepository paymentRepository,
      OrderRepository orderRepository,
      ShipmentRepository shipmentRepository) {
    this.paymentRepository = paymentRepository;
    this.orderRepository = orderRepository;
    this.shipmentRepository = shipmentRepository;
  }

  /**
   * Busca pagos de forma paginada aplicando los filtros de {@link PaymentSpecs}.
   *
   * @param paymentSpecs filtros opcionales de búsqueda
   * @param pageable configuración de página y orden
   * @return la página de pagos encontrados
   */
  @Transactional(readOnly = true)
  public PagedResponse<PaymentResponse> findAll(PaymentSpecs paymentSpecs, Pageable pageable) {
    Specification<Payment> spec =
        SpecificationBuilder.<Payment>builder()
            .equalsIfPresent(root -> root.get(Payment_.method), paymentSpecs.getMethod())
            .betweenIfPresent(
                root -> root.get(Payment_.amount),
                paymentSpecs.getMinAmount(),
                paymentSpecs.getMaxAmount())
            .equalsIfPresent(root -> root.get(Payment_.status), paymentSpecs.getStatus())
            .betweenDatesIfPresent(
                root -> root.get(Payment_.createdAt),
                paymentSpecs.getStartDate(),
                paymentSpecs.getEndDate())
            .likeIfPresent(
                root -> root.get(Payment_.transactionId), paymentSpecs.getTransactionId())
            .equalsIfPresent(
                root -> root.join(Payment_.order).get(Order_.id), paymentSpecs.getOrderId())
            .build();

    Page<Payment> payments = paymentRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        payments.map(PaymentResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un pago por su id.
   *
   * @param paymentId id del pago a buscar
   * @return el pago encontrado
   */
  @Transactional(readOnly = true)
  public PaymentResponse findById(Long paymentId) {
    Payment paymentById = findPaymentByIdOrFail(paymentId);
    return PaymentResponseMapper.INSTANCE.toDto(paymentById);
  }

  /**
   * Registra el pago de una orden con estado {@code COMPLETED}, marca la orden como {@code
   * CONFIRMED} y crea su envío si todavía no existe. Rechaza registrar un segundo pago para la
   * misma orden.
   *
   * @param request datos del pago a registrar
   * @return el pago creado
   */
  @Transactional
  public PaymentResponse create(CreatePaymentRequest request) {
    Order orderById = findOrderByIdOrFail(request.getOrderId());

    if (paymentRepository.existsByOrderId(orderById.getId())) {
      throw new ResourceAlreadyExistsException(AppMessages.PAYMENT_ALREADY_EXISTS_EXCEPTION);
    }

    LocalDateTime now = LocalDateTime.now();
    Payment payment =
        Payment.builder()
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

  /**
   * Actualiza el estado de un pago y refleja el cambio en el estado de su orden asociada.
   *
   * @param paymentId id del pago a actualizar
   * @param request nuevo estado del pago
   */
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

  /**
   * Resuelve el estado que debe tomar una orden a partir del nuevo estado de su pago.
   *
   * @param paymentStatus nuevo estado del pago
   * @param currentStatus estado actual de la orden, usado cuando el pago queda {@code PENDING}
   * @return el estado que debe tomar la orden
   */
  private OrderStatus resolveOrderStatusFromPayment(
      PaymentStatus paymentStatus, OrderStatus currentStatus) {
    return switch (paymentStatus) {
      case COMPLETED -> OrderStatus.CONFIRMED;
      case FAILED, CANCELLED -> OrderStatus.CANCELLED;
      case REFUNDED -> OrderStatus.REFUNDED;
      case PENDING -> currentStatus == null ? OrderStatus.PENDING : currentStatus;
    };
  }

  /**
   * Crea un envío con estado {@code PENDING} para la orden si todavía no tiene uno.
   *
   * @param order orden para la que se crea el envío
   */
  private void createShipmentIfMissing(Order order) {
    if (shipmentRepository.findByOrderId(order.getId()).isPresent()) {
      return;
    }

    String attentionName =
        order.getCustomer() == null
            ? null
            : order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();

    LocalDateTime now = LocalDateTime.now();
    Shipment shipment =
        Shipment.builder()
            .attentionName(attentionName)
            .address(order.getShippingAddress())
            .status(ShipmentStatus.PENDING)
            .order(order)
            .createdAt(now)
            .updatedAt(now)
            .build();

    shipmentRepository.save(shipment);
  }

  /**
   * Genera un identificador de transacción único con el prefijo {@code PAY-}.
   *
   * @return el identificador de transacción generado
   */
  private String generateTransactionId() {
    return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }

  /**
   * Busca un pago por id o lanza {@code 404 Not Found} si no existe.
   *
   * @param paymentId id del pago a buscar
   * @return el pago encontrado
   */
  private Payment findPaymentByIdOrFail(Long paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new ResourceNotFoundException(AppMessages.PAYMENT_NOT_FOUND_EXCEPTION));
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
        .orElseThrow(() -> new ResourceNotFoundException(AppMessages.ORDER_NOT_FOUND_EXCEPTION));
  }
}
