package com.carlosarroyoam.rest.books.order;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.rest.books.core.specification.SpecificationBuilder;
import com.carlosarroyoam.rest.books.customer.CustomerRepository;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import com.carlosarroyoam.rest.books.customer.entity.Customer_;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderItemRequest;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderRequest;
import com.carlosarroyoam.rest.books.order.dto.OrderResponse;
import com.carlosarroyoam.rest.books.order.dto.OrderResponse.OrderResponseMapper;
import com.carlosarroyoam.rest.books.order.dto.OrderSpecs;
import com.carlosarroyoam.rest.books.order.dto.UpdateOrderRequest;
import com.carlosarroyoam.rest.books.order.entity.Order;
import com.carlosarroyoam.rest.books.order.entity.OrderItem;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.carlosarroyoam.rest.books.order.entity.Order_;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
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

/**
 * Contiene la lógica de negocio de {@link Order}: búsqueda paginada con filtros, alta calculando
 * subtotal, impuestos y total a partir de los {@link OrderItem}, y actualización de datos de envío
 * y facturación.
 */
@Service
public class OrderService {
  private static final Logger log = LoggerFactory.getLogger(OrderService.class);
  private static final BigDecimal TAX_RATE = new BigDecimal("0.16");
  private static final BigDecimal DEFAULT_SHIPPING_AMOUNT = new BigDecimal("0.00");

  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final BookRepository bookRepository;

  public OrderService(
      OrderRepository orderRepository,
      CustomerRepository customerRepository,
      BookRepository bookRepository) {
    this.orderRepository = orderRepository;
    this.customerRepository = customerRepository;
    this.bookRepository = bookRepository;
  }

  /**
   * Busca órdenes de forma paginada aplicando los filtros de {@link OrderSpecs}.
   *
   * @param orderSpecs filtros opcionales de búsqueda
   * @param pageable configuración de página y orden
   * @return la página de órdenes encontradas
   */
  @Transactional(readOnly = true)
  public PagedResponse<OrderResponse> findAll(OrderSpecs orderSpecs, Pageable pageable) {
    Specification<Order> spec =
        SpecificationBuilder.<Order>builder()
            .likeIfPresent(root -> root.get(Order_.orderNumber), orderSpecs.getOrderNumber())
            .likeIfPresent(
                root -> root.get(Order_.shippingAddress), orderSpecs.getShippingAddress())
            .betweenIfPresent(
                root -> root.get(Order_.total), orderSpecs.getMinTotal(), orderSpecs.getMaxTotal())
            .equalsIfPresent(root -> root.get(Order_.status), orderSpecs.getStatus())
            .betweenDatesIfPresent(
                root -> root.get(Order_.createdAt),
                orderSpecs.getStartDate(),
                orderSpecs.getEndDate())
            .equalsIfPresent(
                root -> root.join(Order_.customer).get(Customer_.id), orderSpecs.getCustomerId())
            .build();

    Page<Order> orders = orderRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        orders.map(OrderResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca una orden por su id.
   *
   * @param orderId id de la orden a buscar
   * @return la orden encontrada
   */
  @Transactional(readOnly = true)
  public OrderResponse findById(Long orderId) {
    Order orderById = findOrderByIdOrFail(orderId);
    return OrderResponseMapper.INSTANCE.toDto(orderById);
  }

  /**
   * Crea una orden con estado {@code PENDING}, calculando sus ítems, subtotal, impuestos y total.
   *
   * @param request datos de la orden a crear
   * @return la orden creada
   */
  @Transactional
  public OrderResponse create(CreateOrderRequest request) {
    Customer customerById = findCustomerByIdOrFail(request);

    LocalDateTime now = LocalDateTime.now();
    Order order =
        Order.builder()
            .orderNumber(generateOrderNumber())
            .shippingAddress(request.getShippingAddress())
            .billingAddress(request.getBillingAddress())
            .notes(request.getNotes())
            .status(OrderStatus.PENDING)
            .customer(customerById)
            .createdAt(now)
            .updatedAt(now)
            .build();

    List<OrderItem> items = buildOrderItems(request.getItems(), now, order);
    BigDecimal subtotal = calculateSubtotal(items);
    BigDecimal taxAmount = calculateTaxAmount(subtotal);
    BigDecimal shippingAmount = calculateShippingAmount();
    BigDecimal total = calculateTotal(subtotal, taxAmount, shippingAmount);

    order.setItems(items);
    order.setSubtotal(subtotal);
    order.setTaxAmount(taxAmount);
    order.setShippingAmount(shippingAmount);
    order.setTotal(total);

    return OrderResponseMapper.INSTANCE.toDto(orderRepository.save(order));
  }

  /**
   * Actualiza la dirección de envío, la dirección de facturación y las notas de una orden.
   *
   * @param orderId id de la orden a actualizar
   * @param request nuevos datos de la orden
   */
  @Transactional
  public void update(Long orderId, UpdateOrderRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Order orderById = findOrderByIdOrFail(orderId);
    orderById.setShippingAddress(request.getShippingAddress());
    orderById.setBillingAddress(request.getBillingAddress());
    orderById.setNotes(request.getNotes());
    orderById.setUpdatedAt(now);
    orderRepository.save(orderById);
  }

  /**
   * Construye los ítems de una orden a partir de los libros solicitados, fijando el precio unitario
   * y total de cada uno según el precio actual del libro.
   *
   * @param requestItems libros y cantidades solicitados
   * @param now fecha y hora a registrar en cada ítem
   * @param order orden a la que pertenecerán los ítems
   * @return los ítems construidos
   */
  private List<OrderItem> buildOrderItems(
      List<CreateOrderItemRequest> requestItems, LocalDateTime now, Order order) {
    return requestItems.stream()
        .map(
            item -> {
              Book bookById = findBookByIdOrFail(item.getBookId());

              BigDecimal unitPrice = bookById.getPrice().setScale(2, RoundingMode.HALF_UP);
              BigDecimal totalPrice =
                  unitPrice
                      .multiply(BigDecimal.valueOf(item.getQuantity()))
                      .setScale(2, RoundingMode.HALF_UP);

              return OrderItem.builder()
                  .quantity(item.getQuantity())
                  .unitPrice(unitPrice)
                  .totalPrice(totalPrice)
                  .book(bookById)
                  .order(order)
                  .createdAt(now)
                  .updatedAt(now)
                  .build();
            })
        .toList();
  }

  /**
   * Calcula el subtotal de una orden como la suma del precio total de sus ítems.
   *
   * @param items ítems de la orden
   * @return el subtotal calculado
   */
  private BigDecimal calculateSubtotal(List<OrderItem> items) {
    return items.stream()
        .map(OrderItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calcula el impuesto de una orden aplicando la tasa fija {@link #TAX_RATE} al subtotal.
   *
   * @param subtotal subtotal de la orden
   * @return el monto de impuesto calculado
   */
  private BigDecimal calculateTaxAmount(BigDecimal subtotal) {
    return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calcula el costo de envío de una orden.
   *
   * @return el costo de envío, actualmente fijo en {@link #DEFAULT_SHIPPING_AMOUNT}
   */
  private BigDecimal calculateShippingAmount() {
    return DEFAULT_SHIPPING_AMOUNT.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calcula el total de una orden como la suma de subtotal, impuestos y envío.
   *
   * @param subtotal subtotal de la orden
   * @param taxAmount monto de impuesto de la orden
   * @param shippingAmount costo de envío de la orden
   * @return el total calculado
   */
  private BigDecimal calculateTotal(
      BigDecimal subtotal, BigDecimal taxAmount, BigDecimal shippingAmount) {
    return subtotal.add(taxAmount).add(shippingAmount).setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Genera un número de orden único con el prefijo {@code ORD-}.
   *
   * @return el número de orden generado
   */
  private String generateOrderNumber() {
    return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
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

  /**
   * Busca el cliente de una orden por id o lanza {@code 404 Not Found} si no existe.
   *
   * @param request datos de la orden, con el id del cliente a buscar
   * @return el cliente encontrado
   */
  private Customer findCustomerByIdOrFail(CreateOrderRequest request) {
    return customerRepository
        .findById(request.getCustomerId())
        .orElseThrow(
            () -> {
              log.warn(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
            });
  }

  /**
   * Busca un libro por id o lanza {@code 404 Not Found} si no existe.
   *
   * @param bookId id del libro a buscar
   * @return el libro encontrado
   */
  private Book findBookByIdOrFail(Long bookId) {
    return bookRepository
        .findById(bookId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION);
            });
  }
}
