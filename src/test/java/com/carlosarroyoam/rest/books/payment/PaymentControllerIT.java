package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.common.JsonUtils;
import com.carlosarroyoam.rest.books.payment.dto.CreatePaymentRequest;
import com.carlosarroyoam.rest.books.payment.dto.UpdatePaymentStatusRequest;
import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PaymentControllerIT {
  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .defaultRequest(get("/").with(jwt().jwt(jwt -> jwt.claim("preferred_username", "carroyom"))
            .authorities(new SimpleGrantedAuthority("ROLE_App/Admin"))))
        .build();
  }

  @Test
  @DisplayName("GET /payments - Given payments exist, when find all, then returns paged payments")
  void givenPaymentsExist_whenFindAllPayments_thenReturnsPagedPayments() throws Exception {
    String expectedJson = JsonUtils.readJson("/payments/find-all.json");

    String responseJson = mockMvc.perform(get("/payments").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /payments/{id} - Given payment exists, when find by id, then returns payment")
  void givenPaymentExists_whenFindPaymentById_thenReturnsPayment() throws Exception {
    String expectedJson = JsonUtils.readJson("/payments/find-by-id.json");

    String responseJson = mockMvc.perform(get("/payments/{paymentId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("POST /payments - Given order with existing payment, when create, then returns bad request")
  void givenOrderWithExistingPayment_whenCreatePayment_thenReturnsBadRequest() throws Exception {
    CreatePaymentRequest request = CreatePaymentRequest.builder()
        .orderId(1L)
        .method(PaymentMethod.CREDIT_CARD)
        .build();

    mockMvc.perform(post("/payments").contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(request))).andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PUT /payments/{id}/status - Given valid status, when update, then returns no content")
  void givenValidStatus_whenUpdatePaymentStatus_thenReturnsNoContent() throws Exception {
    UpdatePaymentStatusRequest request = UpdatePaymentStatusRequest.builder()
        .status(PaymentStatus.COMPLETED)
        .build();

    mockMvc.perform(put("/payments/{paymentId}/status", 1L).contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(request))).andExpect(status().isNoContent());
  }
}