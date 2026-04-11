package com.carlosarroyoam.rest.books.order;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.customer.CustomerRepository;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderItemRequest;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderRequest;
import com.carlosarroyoam.rest.books.order.dto.OrderResponse;
import com.carlosarroyoam.rest.books.order.dto.OrderSpecs;
import com.carlosarroyoam.rest.books.order.dto.UpdateOrderRequest;
import com.carlosarroyoam.rest.books.order.entity.Order;
import com.carlosarroyoam.rest.books.order.entity.OrderItem;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
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
        .customer(customer)
        .shippingAddress("123 Main Street, Springfield")
        .billingAddress("123 Main Street, Springfield")
        .notes("Leave at the door")
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  @Test
  @DisplayName("Given orders exist, when find all, then returns paged orders")
  void givenOrdersExist_whenFindAll_thenReturnsPagedOrders() {
    Pageable pageable = PageRequest.of(0, 25);
    List<Order> orders = List.of(order);

    when(orderRepository.findAll(ArgumentMatchers.<Specification<Order>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(orders, pageable, orders.size()));

    PagedResponse<OrderResponse> response = orderService.findAll(OrderSpecs.builder().build(),
        pageable);

    assertThat(response).isNotNull();
    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().get(0).getId()).isEqualTo(1L);
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(25);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given order exists, when find by id, then returns order")
  void givenOrderExists_whenFindById_thenReturnsOrder() {
    when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

    OrderResponse orderResponse = orderService.findById(1L);

    assertThat(orderResponse).isNotNull();
    assertThat(orderResponse.getId()).isEqualTo(1L);
    assertThat(orderResponse.getOrderNumber()).isEqualTo("ORD-12345678");
  }

  @Test
  @DisplayName("Given order does not exist, when find by id, then throws not found exception")
  void givenOrderDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.findById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given valid order data, when create, then returns created order")
  void givenValidOrderData_whenCreate_thenReturnsCreatedOrder() {
    CreateOrderRequest request = CreateOrderRequest.builder()
        .customerId(1L)
        .shippingAddress("123 Main Street, Springfield")
        .billingAddress("123 Main Street, Springfield")
        .notes("Leave at the door")
        .items(List.of(CreateOrderItemRequest.builder().bookId(1L).quantity(2).build()))
        .build();

    when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order savedOrder = invocation.getArgument(0);
      savedOrder.setId(1L);
      savedOrder.getItems().forEach(item -> item.setId(1L));
      return savedOrder;
    });

    OrderResponse orderResponse = orderService.create(request);

    assertThat(orderResponse).isNotNull();
    assertThat(orderResponse.getId()).isEqualTo(1L);
    assertThat(orderResponse.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(orderResponse.getSubtotal()).isEqualByComparingTo("45.98");
    assertThat(orderResponse.getTaxAmount()).isEqualByComparingTo("7.36");
    assertThat(orderResponse.getShippingAmount()).isEqualByComparingTo("0.00");
    assertThat(orderResponse.getTotal()).isEqualByComparingTo("53.34");
    assertThat(orderResponse.getItems()).hasSize(1);
    assertThat(orderResponse.getItems().get(0).getUnitPrice()).isEqualByComparingTo("22.99");
  }

  @Test
  @DisplayName("Given customer does not exist, when create, then throws not found exception")
  void givenCustomerDoesNotExist_whenCreate_thenThrowsNotFoundException() {
    CreateOrderRequest request = CreateOrderRequest.builder()
        .customerId(99L)
        .shippingAddress("123 Main Street, Springfield")
        .billingAddress("123 Main Street, Springfield")
        .items(List.of(CreateOrderItemRequest.builder().bookId(1L).quantity(1).build()))
        .build();

    when(customerRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.create(request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given book does not exist, when create, then throws not found exception")
  void givenBookDoesNotExist_whenCreate_thenThrowsNotFoundException() {
    CreateOrderRequest request = CreateOrderRequest.builder()
        .customerId(1L)
        .shippingAddress("123 Main Street, Springfield")
        .billingAddress("123 Main Street, Springfield")
        .items(List.of(CreateOrderItemRequest.builder().bookId(99L).quantity(1).build()))
        .build();

    when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    when(bookRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.create(request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given order exists, when update with valid data, then updates order")
  void givenOrderExists_whenUpdateWithValidData_thenUpdatesOrder() {
    UpdateOrderRequest request = UpdateOrderRequest.builder()
        .shippingAddress("456 Updated Avenue, Springfield")
        .billingAddress("789 Billing Road, Springfield")
        .notes("Call when arriving")
        .build();

    when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

    orderService.update(1L, request);

    verify(orderRepository).save(any(Order.class));
    assertThat(order.getShippingAddress()).isEqualTo("456 Updated Avenue, Springfield");
    assertThat(order.getBillingAddress()).isEqualTo("789 Billing Road, Springfield");
    assertThat(order.getNotes()).isEqualTo("Call when arriving");
  }

  @Test
  @DisplayName("Given order does not exist, when update, then throws not found exception")
  void givenOrderDoesNotExist_whenUpdate_thenThrowsNotFoundException() {
    when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.update(1L, UpdateOrderRequest.builder().build()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given order exists, when delete, then deletes order")
  void givenOrderExists_whenDelete_thenDeletesOrder() {
    when(orderRepository.existsById(anyLong())).thenReturn(true);

    orderService.deleteById(1L);

    verify(orderRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Given order does not exist, when delete, then throws not found exception")
  void givenOrderDoesNotExist_whenDelete_thenThrowsNotFoundException() {
    when(orderRepository.existsById(anyLong())).thenReturn(false);

    assertThatThrownBy(() -> orderService.deleteById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.ORDER_NOT_FOUND_EXCEPTION);
  }
}
