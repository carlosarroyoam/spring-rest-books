package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PaginationDto;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentDto;
import com.carlosarroyoam.rest.books.shipment.dto.ShipmentSpecsDto;
import com.carlosarroyoam.rest.books.shipment.dto.UpdateShipmentStatusRequestDto;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShipmentControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock
  private ShipmentService shipmentService;

  @InjectMocks
  private ShipmentController shipmentController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    mockMvc = MockMvcBuilders.standaloneSetup(shipmentController)
        .setControllerAdvice(GlobalExceptionHandler.class)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("Should return PagedResponseDto<ShipmentDto> when find all shipments")
  void shouldReturnPagedShipmentsWhenFindAllShipments() throws Exception {
    PagedResponseDto<ShipmentDto> pagedResponse = PagedResponseDto.<ShipmentDto>builder()
        .items(List.of(ShipmentDto.builder().id(1L).status(ShipmentStatus.PENDING).build()))
        .pagination(PaginationDto.builder().page(0).size(25).totalItems(1).totalPages(1).build())
        .build();

    when(shipmentService.findAll(any(Pageable.class), any(ShipmentSpecsDto.class)))
        .thenReturn(pagedResponse);

    mockMvc
        .perform(get("/shipments").queryParam("page", "0")
            .queryParam("size", "25")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(1));
  }

  @Test
  @DisplayName("Should return ShipmentDto when find shipment by id")
  void shouldReturnShipmentDtoWhenFindShipmentById() throws Exception {
    ShipmentDto shipment = ShipmentDto.builder()
        .id(1L)
        .attentionName("Carlos Arroyo")
        .address("123 Main Street, Springfield")
        .status(ShipmentStatus.PENDING)
        .orderId(1L)
        .build();

    when(shipmentService.findById(anyLong())).thenReturn(shipment);

    mockMvc.perform(get("/shipments/{shipmentId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.attentionName").value("Carlos Arroyo"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @DisplayName("Should return no content when update shipment status")
  void shouldReturnNoContentWhenUpdateShipmentStatus() throws Exception {
    UpdateShipmentStatusRequestDto requestDto = UpdateShipmentStatusRequestDto.builder()
        .status(ShipmentStatus.SHIPPED)
        .build();

    mockMvc.perform(
        put("/shipments/{shipmentId}/status", 1L).content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }
}
