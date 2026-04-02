package com.carlosarroyoam.rest.books.orders;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto.PagedResponseDtoMapper;
import com.carlosarroyoam.rest.books.customer.CustomerRepository;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import com.carlosarroyoam.rest.books.orders.dto.CreateOrderItemRequestDto;
import com.carlosarroyoam.rest.books.orders.dto.CreateOrderRequestDto;
import com.carlosarroyoam.rest.books.orders.dto.OrderDto;
import com.carlosarroyoam.rest.books.orders.dto.OrderDto.OrderDtoMapper;
import com.carlosarroyoam.rest.books.orders.dto.UpdateOrderRequestDto;
import com.carlosarroyoam.rest.books.orders.entity.Order;
import com.carlosarroyoam.rest.books.orders.entity.OrderItem;
import com.carlosarroyoam.rest.books.orders.entity.OrderStatus;
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
  public PagedResponseDto<OrderDto> findAll(Pageable pageable) {
    Page<Order> orders = orderRepository.findAll(pageable);
    return PagedResponseDtoMapper.INSTANCE
        .toPagedResponseDto(orders.map(OrderDtoMapper.INSTANCE::toDto));
  }

  @Transactional
  public OrderDto findById(Long orderId) {
    Order order = findOrderEntityById(orderId);
    return OrderDtoMapper.INSTANCE.toDto(order);
  }

  @Transactional
  public OrderDto create(CreateOrderRequestDto requestDto) {
    Customer customer = customerRepository.findById(requestDto.getCustomerId()).orElseThrow(() -> {
      log.warn(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
    });

    LocalDateTime now = LocalDateTime.now();
    Order order = Order.builder()
        .notes(requestDto.getNotes())
        .shippingAddress(requestDto.getShippingAddress())
        .billingAddress(requestDto.getBillingAddress())
        .build();
    List<OrderItem> items = buildOrderItems(requestDto.getItems(), now, order);
    BigDecimal subtotal = calculateSubtotal(items);
    BigDecimal taxAmount = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    BigDecimal shippingAmount = DEFAULT_SHIPPING_AMOUNT.setScale(2, RoundingMode.HALF_UP);
    BigDecimal total = subtotal.add(taxAmount)
        .add(shippingAmount)
        .setScale(2, RoundingMode.HALF_UP);

    order.setOrderNumber(generateOrderNumber());
    order.setStatus(OrderStatus.PENDING);
    order.setCustomerId(customer.getId());
    order.setCustomer(customer);
    order.setItems(items);
    order.setSubtotal(subtotal);
    order.setTaxAmount(taxAmount);
    order.setShippingAmount(shippingAmount);
    order.setTotal(total);
    order.setCreatedAt(now);
    order.setUpdatedAt(now);

    return OrderDtoMapper.INSTANCE.toDto(orderRepository.save(order));
  }

  @Transactional
  public void update(Long orderId, UpdateOrderRequestDto requestDto) {
    Order order = findOrderEntityById(orderId);
    order.setShippingAddress(requestDto.getShippingAddress());
    order.setBillingAddress(requestDto.getBillingAddress());
    order.setNotes(requestDto.getNotes());
    order.setUpdatedAt(LocalDateTime.now());
    orderRepository.save(order);
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
          .bookId(book.getId())
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

  private String generateOrderNumber() {
    return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
  }
}
