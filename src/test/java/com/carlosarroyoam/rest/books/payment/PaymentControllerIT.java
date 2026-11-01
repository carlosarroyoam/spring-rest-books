package com.carlosarroyoam.rest.books.payment;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.payment.dto.CreatePaymentRequest;
import com.carlosarroyoam.rest.books.payment.dto.UpdatePaymentStatusRequest;
import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import com.carlosarroyoam.rest.books.support.testutils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentControllerIT {
  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper mapper;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .defaultRequest(
                get("/")
                    .with(
                        jwt()
                            .jwt(jwt -> jwt.claim("preferred_username", "carroyom"))
                            .authorities(new SimpleGrantedAuthority("ROLE_App/Admin"))))
            .build();
  }

  @Test
  @DisplayName("GET /payments - Given payments exist, when find all, then returns paged payments")
  void givenPaymentsExist_whenFindAllPayments_thenReturnsPagedPayments() throws Exception {
    String expectedJson = JsonUtils.readJson("payments/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/payments").param("page", "0").param("size", "25"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /payments - Given anonymous user, when find all, then returns unauthorized")
  void givenAnonymousUser_whenFindAllPayments_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/payments").with(anonymous()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/payments"));
  }

  @Test
  @DisplayName("GET /payments - Given customer role, when find all, then returns forbidden")
  void givenCustomerRole_whenFindAllPayments_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/payments").with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/payments"));
  }

  @Test
  @DisplayName("GET /payments/{id} - Given payment exists, when find by id, then returns payment")
  void givenPaymentExists_whenFindPaymentById_thenReturnsPayment() throws Exception {
    String expectedJson = JsonUtils.readJson("payments/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/payments/{paymentId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /payments/{id} - Given customer role, when find by id, then returns forbidden")
  void givenCustomerRole_whenFindPaymentById_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/payments/{paymentId}", 1L).with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/payments/1"));
  }

  @Test
  @DisplayName(
      "GET /payments/{id} - Given payment does not exist, when find by id, then returns not found")
  void givenPaymentDoesNotExist_whenFindPaymentById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/payments/{paymentId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Payment not found"))
        .andExpect(jsonPath("$.instance").value("/payments/9999"));
  }

  @Test
  @DisplayName(
      "POST /payments - Given order with existing payment, when create, then returns conflict")
  void givenOrderWithExistingPayment_whenCreatePayment_thenReturnsConflict() throws Exception {
    CreatePaymentRequest request =
        CreatePaymentRequest.builder().orderId(1L).method(PaymentMethod.CREDIT_CARD).build();

    mockMvc
        .perform(
            post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Conflict"))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.detail").isNotEmpty())
        .andExpect(jsonPath("$.instance").value("/payments"))
        .andExpect(jsonPath("$.timestamp").doesNotExist());
  }

  @Test
  @DisplayName("POST /payments - Given customer role, when create, then returns forbidden")
  void givenCustomerRole_whenCreatePayment_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            post("/payments")
                .with(customerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest(1L))))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/payments"));
  }

  @Test
  @DisplayName("POST /payments - Given order does not exist, when create, then returns not found")
  void givenOrderDoesNotExist_whenCreatePayment_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest(9999L))))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Order not found"))
        .andExpect(jsonPath("$.instance").value("/payments"));
  }

  @Test
  @DisplayName(
      "POST /payments - Given invalid payment data, when create, then returns problem detail")
  void givenInvalidPaymentData_whenCreatePayment_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(post("/payments").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/payments"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.orderId").isNotEmpty())
        .andExpect(jsonPath("$.errors.method").isNotEmpty());
  }

  @Test
  @DisplayName(
      "PUT /payments/{id}/status - Given valid status, when update, then returns no content")
  void givenValidStatus_whenUpdatePaymentStatus_thenReturnsNoContent() throws Exception {
    UpdatePaymentStatusRequest request =
        UpdatePaymentStatusRequest.builder().status(PaymentStatus.COMPLETED).build();

    mockMvc
        .perform(
            put("/payments/{paymentId}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "PUT /payments/{id}/status - Given customer role, when update, then returns forbidden")
  void givenCustomerRole_whenUpdatePaymentStatus_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            put("/payments/{paymentId}/status", 1L)
                .with(customerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validStatusRequest())))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/payments/1/status"));
  }

  @Test
  @DisplayName(
      "PUT /payments/{id}/status - Given payment does not exist, when update, then returns not"
          + " found")
  void givenPaymentDoesNotExist_whenUpdatePaymentStatus_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            put("/payments/{paymentId}/status", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validStatusRequest())))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Payment not found"))
        .andExpect(jsonPath("$.instance").value("/payments/9999/status"));
  }

  @Test
  @DisplayName(
      "PUT /payments/{id}/status - Given invalid status, when update, then returns problem detail")
  void givenInvalidStatus_whenUpdatePaymentStatus_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(
            put("/payments/{paymentId}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/payments/1/status"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.status").isNotEmpty());
  }

  private static RequestPostProcessor customerJwt() {
    return jwt()
        .jwt(jwt -> jwt.claim("preferred_username", "customer"))
        .authorities(new SimpleGrantedAuthority("ROLE_App/Customer"));
  }

  private static CreatePaymentRequest createRequest(Long orderId) {
    return CreatePaymentRequest.builder()
        .orderId(orderId)
        .method(PaymentMethod.CREDIT_CARD)
        .build();
  }

  private static UpdatePaymentStatusRequest validStatusRequest() {
    return UpdatePaymentStatusRequest.builder().status(PaymentStatus.COMPLETED).build();
  }
}
