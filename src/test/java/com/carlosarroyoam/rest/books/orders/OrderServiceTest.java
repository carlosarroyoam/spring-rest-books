package com.carlosarroyoam.rest.books.orders;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.customer.CustomerRepository;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import com.carlosarroyoam.rest.books.orders.dto.CreateOrderItemRequestDto;
import com.carlosarroyoam.rest.books.orders.dto.CreateOrderRequestDto;
import com.carlosarroyoam.rest.books.orders.dto.OrderDto;
import com.carlosarroyoam.rest.books.orders.dto.UpdateOrderRequestDto;
import com.carlosarroyoam.rest.books.orders.entity.Order;
import com.carlosarroyoam.rest.books.orders.entity.OrderItem;
import com.carlosarroyoam.rest.books.orders.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
  @Mock
  private OrderRepository orderRepository;

  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private OrderService orderService;

  private Order order;
  private Customer customer;
  private Book book;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    customer = Customer.builder()
        .id(1L)
        .firstName("Carlos")
        .lastName("Arroyo")
        .email("carroyom@mail.com")
        .username("carroyom")
        .createdAt(now)
        .updatedAt(now)
        .build();

    book = Book.builder()
        .id(1L)
        .isbn("978-1-3035-0529-4")
        .title("Homo Deus")
        .coverUrl("https://example.com/homo-deus.jpg")
        .price(new BigDecimal("22.99"))
        .isAvailableOnline(Boolean.TRUE)
        .publishedAt(now.toLocalDate())
        .createdAt(now)
        .updatedAt(now)
        .build();

    OrderItem orderItem = OrderItem.builder()
        .id(1L)
        .quantity(2)
        .unitPrice(new BigDecimal("22.99"))
        .totalPrice(new BigDecimal("45.98"))
        .bookId(1L)
        .book(book)
        .createdAt(now)
        .updatedAt(now)
        .build();

    order = Order.builder()
        .id(1L)
        .orderNumber("ORD-12345678")
        .status(OrderStatus.PENDING)
        .items(List.of(orderItem))
        .subtotal(new BigDecimal("45.98"))
        .taxAmount(new BigDecimal("7.36"))
        .shippingAmount(new BigDecimal("0.00"))
        .total(new BigDecimal("53.34"))
        .customerId(1L)
        .customer(customer)
        .shippingAddress("123 Main Street, Springfield")
        .billingAddress("123 Main Street, Springfield")
        .notes("Leave at the door")
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  @Test
  @DisplayName("Should return PagedResponseDto<OrderDto> when find all orders")
  void shouldReturnListOfOrders() {
    Pageable pageable = PageRequest.of(0, 25);
    List<Order> orders = List.of(order);

    when(orderRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(orders, pageable, orders.size()));

    PagedResponseDto<OrderDto> response = orderService.findAll(pageable);

    assertThat(response).isNotNull();
    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().get(0).getId()).isEqualTo(1L);
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(25);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should return OrderDto when find order by id with existing id")
  void shouldReturnWhenFindOrderByIdWithExistingId() {
    when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

    OrderDto orderDto = orderService.findById(1L);

    assertThat(orderDto).isNotNull();
    assertThat(orderDto.getId()).isEqualTo(1L);
    assertThat(orderDto.getOrderNumber()).isEqualTo("ORD-12345678");
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find an order by id with non existing id")
  void shouldThrowWhenFindOrderByIdWithNonExistingId() {
    when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.findById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return OrderDto when create an order with valid data")
  void shouldReturnWhenCreateOrderWithValidData() {
    CreateOrderRequestDto requestDto = CreateOrderRequestDto.builder()
        .customerId(1L)
        .shippingAddress("123 Main Street, Springfield")
        .billingAddress("123 Main Street, Springfield")
        .notes("Leave at the door")
        .items(List.of(CreateOrderItemRequestDto.builder().bookId(1L).quantity(2).build()))
        .build();

    when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order savedOrder = invocation.getArgument(0);
      savedOrder.setId(1L);
      savedOrder.getItems().forEach(item -> item.setId(1L));
      return savedOrder;
    });

    OrderDto orderDto = orderService.create(requestDto);

    assertThat(orderDto).isNotNull();
    assertThat(orderDto.getId()).isEqualTo(1L);
    assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(orderDto.getSubtotal()).isEqualByComparingTo("45.98");
    assertThat(orderDto.getTaxAmount()).isEqualByComparingTo("7.36");
    assertThat(orderDto.getShippingAmount()).isEqualByComparingTo("0.00");
    assertThat(orderDto.getTotal()).isEqualByComparingTo("53.34");
    assertThat(orderDto.getItems()).hasSize(1);
    assertThat(orderDto.getItems().get(0).getUnitPrice()).isEqualByComparingTo("22.99");
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create an order with non existing customer")
  void shouldThrowWhenCreateOrderWithNonExistingCustomer() {
    CreateOrderRequestDto requestDto = CreateOrderRequestDto.builder()
        .customerId(99L)
        .shippingAddress("123 Main Street, Springfield")
        .billingAddress("123 Main Street, Springfield")
        .items(List.of(CreateOrderItemRequestDto.builder().bookId(1L).quantity(1).build()))
        .build();

    when(customerRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create an order with non existing book")
  void shouldThrowWhenCreateOrderWithNonExistingBook() {
    CreateOrderRequestDto requestDto = CreateOrderRequestDto.builder()
        .customerId(1L)
        .shippingAddress("123 Main Street, Springfield")
        .billingAddress("123 Main Street, Springfield")
        .items(List.of(CreateOrderItemRequestDto.builder().bookId(99L).quantity(1).build()))
        .build();

    when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    when(bookRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should update order with valid data")
  void shouldUpdateOrderWithValidData() {
    UpdateOrderRequestDto requestDto = UpdateOrderRequestDto.builder()
        .shippingAddress("456 Updated Avenue, Springfield")
        .billingAddress("789 Billing Road, Springfield")
        .notes("Call when arriving")
        .build();

    when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

    orderService.update(1L, requestDto);

    verify(orderRepository).save(any(Order.class));
    assertThat(order.getShippingAddress()).isEqualTo("456 Updated Avenue, Springfield");
    assertThat(order.getBillingAddress()).isEqualTo("789 Billing Road, Springfield");
    assertThat(order.getNotes()).isEqualTo("Call when arriving");
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update order with non existing id")
  void shouldThrowWhenUpdateOrderWithNonExistingId() {
    when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.update(1L, UpdateOrderRequestDto.builder().build()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should delete order with existing id")
  void shouldDeleteOrderWithExistingId() {
    when(orderRepository.existsById(anyLong())).thenReturn(true);

    orderService.deleteById(1L);

    verify(orderRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete order with non existing id")
  void shouldThrowWhenDeleteOrderWithNonExistingId() {
    when(orderRepository.existsById(anyLong())).thenReturn(false);

    assertThatThrownBy(() -> orderService.deleteById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
  }
}
