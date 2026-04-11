package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.common.JsonUtils;
import com.carlosarroyoam.rest.books.shipment.dto.UpdateShipmentStatusRequest;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ShipmentControllerIT {
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
  @DisplayName("GET /shipments - Given shipments exist, when find all, then returns paged shipments")
  void givenShipmentsExist_whenFindAllShipments_thenReturnsPagedShipments() throws Exception {
    String expectedJson = JsonUtils.readJson("/shipments/find-all.json");

    String responseJson = mockMvc.perform(get("/shipments").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /shipments/{id} - Given shipment exists, when find by id, then returns shipment")
  void givenShipmentExists_whenFindShipmentById_thenReturnsShipment() throws Exception {
    String expectedJson = JsonUtils.readJson("/shipments/find-by-id.json");

    String responseJson = mockMvc.perform(get("/shipments/{shipmentId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("PUT /shipments/{id}/status - Given valid status, when update, then returns no content")
  void givenValidStatus_whenUpdateShipmentStatus_thenReturnsNoContent() throws Exception {
    UpdateShipmentStatusRequest request = UpdateShipmentStatusRequest.builder()
        .status(ShipmentStatus.DELIVERED)
        .build();

    mockMvc
        .perform(put("/shipments/{shipmentId}/status", 1L).contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }
}