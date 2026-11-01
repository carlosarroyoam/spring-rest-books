package com.carlosarroyoam.rest.books.shipment;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.shipment.dto.UpdateShipmentStatusRequest;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
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
class ShipmentControllerIT {
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
  @DisplayName(
      "GET /shipments - Given shipments exist, when find all, then returns paged shipments")
  void givenShipmentsExist_whenFindAllShipments_thenReturnsPagedShipments() throws Exception {
    String expectedJson = JsonUtils.readJson("shipments/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/shipments").param("page", "0").param("size", "25"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /shipments - Given anonymous user, when find all, then returns unauthorized")
  void givenAnonymousUser_whenFindAllShipments_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/shipments").with(anonymous()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/shipments"));
  }

  @Test
  @DisplayName("GET /shipments - Given customer role, when find all, then returns forbidden")
  void givenCustomerRole_whenFindAllShipments_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/shipments").with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/shipments"));
  }

  @Test
  @DisplayName(
      "GET /shipments/{id} - Given shipment exists, when find by id, then returns shipment")
  void givenShipmentExists_whenFindShipmentById_thenReturnsShipment() throws Exception {
    String expectedJson = JsonUtils.readJson("shipments/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/shipments/{shipmentId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /shipments/{id} - Given customer role, when find by id, then returns forbidden")
  void givenCustomerRole_whenFindShipmentById_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/shipments/{shipmentId}", 1L).with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/shipments/1"));
  }

  @Test
  @DisplayName(
      "GET /shipments/{id} - Given shipment does not exist, when find by id, then returns not"
          + " found")
  void givenShipmentDoesNotExist_whenFindShipmentById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/shipments/{shipmentId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Shipment not found"))
        .andExpect(jsonPath("$.instance").value("/shipments/9999"));
  }

  @Test
  @DisplayName(
      "PUT /shipments/{id}/status - Given valid status, when update, then returns no content")
  void givenValidStatus_whenUpdateShipmentStatus_thenReturnsNoContent() throws Exception {
    UpdateShipmentStatusRequest request =
        UpdateShipmentStatusRequest.builder().status(ShipmentStatus.DELIVERED).build();

    mockMvc
        .perform(
            put("/shipments/{shipmentId}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "PUT /shipments/{id}/status - Given customer role, when update, then returns forbidden")
  void givenCustomerRole_whenUpdateShipmentStatus_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            put("/shipments/{shipmentId}/status", 1L)
                .with(customerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validStatusRequest())))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/shipments/1/status"));
  }

  @Test
  @DisplayName(
      "PUT /shipments/{id}/status - Given shipment does not exist, when update, then returns not"
          + " found")
  void givenShipmentDoesNotExist_whenUpdateShipmentStatus_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            put("/shipments/{shipmentId}/status", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validStatusRequest())))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Shipment not found"))
        .andExpect(jsonPath("$.instance").value("/shipments/9999/status"));
  }

  @Test
  @DisplayName(
      "PUT /shipments/{id}/status - Given invalid status, when update, then returns problem detail")
  void givenInvalidStatus_whenUpdateShipmentStatus_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(
            put("/shipments/{shipmentId}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/shipments/1/status"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.status").isNotEmpty());
  }

  private static RequestPostProcessor customerJwt() {
    return jwt()
        .jwt(jwt -> jwt.claim("preferred_username", "customer"))
        .authorities(new SimpleGrantedAuthority("ROLE_App/Customer"));
  }

  private static UpdateShipmentStatusRequest validStatusRequest() {
    return UpdateShipmentStatusRequest.builder().status(ShipmentStatus.DELIVERED).build();
  }
}
