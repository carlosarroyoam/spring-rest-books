package com.carlosarroyoam.rest.books.order;

import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.core.dto.PaginationResponse;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderItemRequest;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderRequest;
import com.carlosarroyoam.rest.books.order.dto.OrderResponse;
import com.carlosarroyoam.rest.books.order.dto.OrderSpecs;
import com.carlosarroyoam.rest.books.order.dto.UpdateOrderRequest;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock
  private OrderService orderService;

  @InjectMocks
  private OrderController orderController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    mockMvc = MockMvcBuilders.standaloneSetup(orderController)
        .setControllerAdvice(GlobalExceptionHandler.class)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("GET /orders - Given orders exist, when find all, then returns paged orders")
  void givenOrdersExist_whenFindAllOrders_thenReturnsPagedOrders() throws Exception {
    PagedResponse<OrderResponse> pagedResponse = PagedResponse.<OrderResponse>builder()
        .items(List.of(OrderResponse.builder().id(1L).status(OrderStatus.PENDING).build()))
        .pagination(
            PaginationResponse.builder().page(0).size(25).totalItems(1).totalPages(1).build())
        .build();

    when(orderService.findAll(any(OrderSpecs.class), any(Pageable.class)))
        .thenReturn(pagedResponse);

    mockMvc
        .perform(get("/orders").queryParam("page", "0")
            .queryParam("size", "25")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(1))
        .andExpect(jsonPath("$.pagination.page").value(0));
  }

  @Test
  @DisplayName("GET /orders/{id} - Given order exists, when find by id, then returns order")
  void givenOrderExists_whenFindOrderById_thenReturnsOrder() throws Exception {
    OrderResponse order = OrderResponse.builder()
        .id(1L)
        .orderNumber("ORD-12345678")
        .status(OrderStatus.PENDING)
        .total(new BigDecimal("53.34"))
        .build();

    when(orderService.findById(anyLong())).thenReturn(order);

    mockMvc.perform(get("/orders/{orderId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.orderNumber").value("ORD-12345678"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @DisplayName("POST /orders - Given valid order data, when create, then returns created")
  void givenValidOrderData_whenCreateOrder_thenReturnsCreated() throws Exception {
    CreateOrderRequest request = CreateOrderRequest.builder()
        .customerId(1L)
        .shippingAddress("123 Main Street, Springfield")
        .billingAddress("123 Main Street, Springfield")
        .notes("Leave at the door")
        .items(List.of(CreateOrderItemRequest.builder().bookId(1L).quantity(2).build()))
        .build();

    when(orderService.create(any(CreateOrderRequest.class)))
        .thenReturn(OrderResponse.builder().id(1L).build());

    mockMvc
        .perform(post("/orders").content(mapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().string("location", "http://localhost/orders/1"));
  }

  @Test
  @DisplayName("PUT /orders/{id} - Given valid order data, when update, then returns no content")
  void givenValidOrderData_whenUpdateOrder_thenReturnsNoContent() throws Exception {
    UpdateOrderRequest request = UpdateOrderRequest.builder()
        .shippingAddress("456 Updated Avenue, Springfield")
        .billingAddress("789 Billing Road, Springfield")
        .notes("Call when arriving")
        .build();

    mockMvc.perform(put("/orders/{orderId}", 1L).content(mapper.writeValueAsString(request))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /orders/{id} - Given order exists, when delete, then returns no content")
  void givenOrderExists_whenDeleteOrder_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/orders/{orderId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }
}
