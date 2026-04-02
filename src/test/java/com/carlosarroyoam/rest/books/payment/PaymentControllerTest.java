package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PaginationDto;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.rest.books.orders.dto.PaymentDto;
import com.carlosarroyoam.rest.books.orders.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.orders.entity.PaymentStatus;
import com.carlosarroyoam.rest.books.payment.dto.CreatePaymentRequestDto;
import com.carlosarroyoam.rest.books.payment.dto.UpdatePaymentStatusRequestDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

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
  @DisplayName("Should return PagedResponseDto<PaymentDto> when find all payments")
  void shouldReturnPagedPaymentsWhenFindAllPayments() throws Exception {
    PagedResponseDto<PaymentDto> pagedResponse = PagedResponseDto.<PaymentDto>builder()
        .items(List.of(PaymentDto.builder().id(1L).status(PaymentStatus.COMPLETED).build()))
        .pagination(PaginationDto.builder().page(0).size(25).totalItems(1).totalPages(1).build())
        .build();

    when(paymentService.findAll(any(Pageable.class))).thenReturn(pagedResponse);

    mockMvc.perform(get("/payments").queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(1));
  }

  @Test
  @DisplayName("Should return PaymentDto when find payment by id")
  void shouldReturnPaymentDtoWhenFindPaymentById() throws Exception {
    PaymentDto payment = PaymentDto.builder()
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
  @DisplayName("Should return created when create a payment")
  void shouldReturnCreatedWhenCreatePayment() throws Exception {
    CreatePaymentRequestDto requestDto = CreatePaymentRequestDto.builder()
        .orderId(1L)
        .method(PaymentMethod.CREDIT_CARD)
        .build();

    when(paymentService.create(any(CreatePaymentRequestDto.class)))
        .thenReturn(PaymentDto.builder().id(1L).build());

    mockMvc.perform(post("/payments").content(mapper.writeValueAsString(requestDto))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().string("location", "http://localhost/payments/1"));
  }

  @Test
  @DisplayName("Should return no content when update payment status")
  void shouldReturnNoContentWhenUpdatePaymentStatus() throws Exception {
    UpdatePaymentStatusRequestDto requestDto = UpdatePaymentStatusRequestDto.builder()
        .status(PaymentStatus.REFUNDED)
        .build();

    mockMvc.perform(put("/payments/{paymentId}/status", 1L)
        .content(mapper.writeValueAsString(requestDto))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }
}
