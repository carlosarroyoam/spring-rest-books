package com.carlosarroyoam.rest.books.order;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto.PagedResponseDtoMapper;
import com.carlosarroyoam.rest.books.core.specification.SpecificationBuilder;
import com.carlosarroyoam.rest.books.customer.CustomerRepository;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import com.carlosarroyoam.rest.books.customer.entity.Customer_;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderItemRequestDto;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderRequestDto;
import com.carlosarroyoam.rest.books.order.dto.OrderDto;
import com.carlosarroyoam.rest.books.order.dto.OrderDto.OrderDtoMapper;
import com.carlosarroyoam.rest.books.order.dto.OrderSpecsDto;
import com.carlosarroyoam.rest.books.order.dto.UpdateOrderRequestDto;
import com.carlosarroyoam.rest.books.order.entity.Order;
import com.carlosarroyoam.rest.books.order.entity.OrderItem;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.carlosarroyoam.rest.books.order.entity.Order_;
import jakarta.transaction.Transactional;
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
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {
  private static final Logger log = LoggerFactory.getLogger(OrderService.class);
  private static final BigDecimal TAX_RATE = new BigDecimal("0.16");
  private static final BigDecimal DEFAULT_SHIPPING_AMOUNT = new BigDecimal("0.00");

  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final BookRepository bookRepository;

  public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository,
      BookRepository bookRepository) {
    this.orderRepository = orderRepository;
    this.customerRepository = customerRepository;
    this.bookRepository = bookRepository;
  }

  @Transactional
  public PagedResponseDto<OrderDto> findAll(OrderSpecsDto orderSpecs, Pageable pageable) {
    Specification<Order> spec = SpecificationBuilder.<Order>builder()
        .likeIfPresent(root -> root.get(Order_.orderNumber), orderSpecs.getOrderNumber())
        .likeIfPresent(root -> root.get(Order_.shippingAddress), orderSpecs.getShippingAddress())
        .betweenIfPresent(root -> root.get(Order_.total), orderSpecs.getMinTotal(),
            orderSpecs.getMaxTotal())
        .equalsIfPresent(root -> root.get(Order_.status), orderSpecs.getStatus())
        .betweenDatesIfPresent(root -> root.get(Order_.createdAt), orderSpecs.getStartDate(),
            orderSpecs.getEndDate())
        .equalsIfPresent(root -> root.join(Order_.customer).get(Customer_.id),
            orderSpecs.getCustomerId())
        .build();

    Page<Order> orders = orderRepository.findAll(spec, pageable);

    return PagedResponseDtoMapper.INSTANCE
        .toPagedResponseDto(orders.map(OrderDtoMapper.INSTANCE::toDto));
  }

  @Transactional
  public OrderDto findById(Long orderId) {
    Order orderById = findOrderEntityById(orderId);
    return OrderDtoMapper.INSTANCE.toDto(orderById);
  }

  @Transactional
  public OrderDto create(CreateOrderRequestDto requestDto) {
    Customer customerById = findCustomerEntityById(requestDto);

    LocalDateTime now = LocalDateTime.now();
    Order order = Order.builder()
        .orderNumber(generateOrderNumber())
        .shippingAddress(requestDto.getShippingAddress())
        .billingAddress(requestDto.getBillingAddress())
        .notes(requestDto.getNotes())
        .status(OrderStatus.PENDING)
        .customer(customerById)
        .createdAt(now)
        .updatedAt(now)
        .build();

    List<OrderItem> items = buildOrderItems(requestDto.getItems(), now, order);
    BigDecimal subtotal = calculateSubtotal(items);
    BigDecimal taxAmount = calculateTaxAmount(subtotal);
    BigDecimal shippingAmount = calculateShippingAmount();
    BigDecimal total = calculateTotal(subtotal, taxAmount, shippingAmount);

    order.setItems(items);
    order.setSubtotal(subtotal);
    order.setTaxAmount(taxAmount);
    order.setShippingAmount(shippingAmount);
    order.setTotal(total);

    return OrderDtoMapper.INSTANCE.toDto(orderRepository.save(order));
  }

  @Transactional
  public void update(Long orderId, UpdateOrderRequestDto requestDto) {
    LocalDateTime now = LocalDateTime.now();
    Order orderById = findOrderEntityById(orderId);
    orderById.setShippingAddress(requestDto.getShippingAddress());
    orderById.setBillingAddress(requestDto.getBillingAddress());
    orderById.setNotes(requestDto.getNotes());
    orderById.setUpdatedAt(now);
    orderRepository.save(orderById);
  }

  @Transactional
  public void deleteById(Long orderId) {
    if (Boolean.FALSE.equals(orderRepository.existsById(orderId))) {
      log.warn(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.ORDER_NOT_FOUND_EXCEPTION);
    }

    orderRepository.deleteById(orderId);
  }

  private Order findOrderEntityById(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> {
      log.warn(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.ORDER_NOT_FOUND_EXCEPTION);
    });
  }

  private Customer findCustomerEntityById(CreateOrderRequestDto requestDto) {
    return customerRepository.findById(requestDto.getCustomerId()).orElseThrow(() -> {
      log.warn(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
    });
  }

  private List<OrderItem> buildOrderItems(List<CreateOrderItemRequestDto> requestItems,
      LocalDateTime now, Order order) {
    return requestItems.stream().map(item -> {
      Book book = bookRepository.findById(item.getBookId()).orElseThrow(() -> {
        log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
            AppMessages.BOOK_NOT_FOUND_EXCEPTION);
      });

      BigDecimal unitPrice = book.getPrice().setScale(2, RoundingMode.HALF_UP);
      BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
          .setScale(2, RoundingMode.HALF_UP);

      return OrderItem.builder()
          .quantity(item.getQuantity())
          .unitPrice(unitPrice)
          .totalPrice(totalPrice)
          .book(book)
          .order(order)
          .createdAt(now)
          .updatedAt(now)
          .build();
    }).toList();
  }

  private BigDecimal calculateSubtotal(List<OrderItem> items) {
    return items.stream()
        .map(OrderItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateTaxAmount(BigDecimal subtotal) {
    return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateShippingAmount() {
    return DEFAULT_SHIPPING_AMOUNT.setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal taxAmount,
      BigDecimal shippingAmount) {
    return subtotal.add(taxAmount).add(shippingAmount).setScale(2, RoundingMode.HALF_UP);
  }

  private String generateOrderNumber() {
    return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
  }
}
