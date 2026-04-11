package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.core.dto.PaginationResponse;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.rest.books.payment.dto.CreatePaymentRequest;
import com.carlosarroyoam.rest.books.payment.dto.PaymentResponse;
import com.carlosarroyoam.rest.books.payment.dto.PaymentSpecs;
import com.carlosarroyoam.rest.books.payment.dto.UpdatePaymentStatusRequest;
import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock
  private PaymentService paymentService;

  @InjectMocks
  private PaymentController paymentController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
        .setControllerAdvice(GlobalExceptionHandler.class)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("GET /payments - Given payments exist, when find all, then returns paged payments")
  void givenPaymentsExist_whenFindAllPayments_thenReturnsPagedPayments() throws Exception {
    PagedResponse<PaymentResponse> pagedResponse = PagedResponse.<PaymentResponse>builder()
        .items(List.of(PaymentResponse.builder().id(1L).status(PaymentStatus.COMPLETED).build()))
        .pagination(
            PaginationResponse.builder().page(0).size(25).totalItems(1).totalPages(1).build())
        .build();

    when(paymentService.findAll(any(PaymentSpecs.class), any(Pageable.class)))
        .thenReturn(pagedResponse);

    mockMvc
        .perform(get("/payments").queryParam("page", "0")
            .queryParam("size", "25")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(1));
  }

  @Test
  @DisplayName("GET /payments/{id} - Given payment exists, when find by id, then returns payment")
  void givenPaymentExists_whenFindPaymentById_thenReturnsPayment() throws Exception {
    PaymentResponse payment = PaymentResponse.builder()
        .id(1L)
        .amount(new BigDecimal("53.34"))
        .method(PaymentMethod.CREDIT_CARD)
        .status(PaymentStatus.COMPLETED)
        .transactionId("PAY-ABC123")
        .orderId(1L)
        .build();

    when(paymentService.findById(anyLong())).thenReturn(payment);

    mockMvc.perform(get("/payments/{paymentId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.method").value("CREDIT_CARD"))
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  @DisplayName("POST /payments - Given valid payment data, when create, then returns created")
  void givenValidPaymentData_whenCreatePayment_thenReturnsCreated() throws Exception {
    CreatePaymentRequest request = CreatePaymentRequest.builder()
        .orderId(1L)
        .method(PaymentMethod.CREDIT_CARD)
        .build();

    when(paymentService.create(any(CreatePaymentRequest.class)))
        .thenReturn(PaymentResponse.builder().id(1L).build());

    mockMvc
        .perform(post("/payments").content(mapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().string("location", "http://localhost/payments/1"));
  }

  @Test
  @DisplayName("PUT /payments/{id}/status - Given valid status, when update, then returns no content")
  void givenValidStatus_whenUpdatePaymentStatus_thenReturnsNoContent() throws Exception {
    UpdatePaymentStatusRequest request = UpdatePaymentStatusRequest.builder()
        .status(PaymentStatus.REFUNDED)
        .build();

    mockMvc
        .perform(put("/payments/{paymentId}/status", 1L).content(mapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }
}
