package com.carlosarroyoam.rest.books.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.common.JsonUtils;
import com.carlosarroyoam.rest.books.user.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UpdateUserRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class UserControllerIT {
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
        .defaultRequest(
            get("/").with(jwt().jwt(jwt -> jwt.claim("preferred_username", "carroyom"))))
        .build();
  }

  @Test
  @DisplayName("Should return List<UserDto> when find all users")
  void shouldReturnListOfUsersWhenFindAllUsers() throws Exception {
    String expectedJson = JsonUtils.readJson("/users/find-all.json");

    String responseJson = mockMvc.perform(get("/users").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should return List<UserDto> when find all users with filters")
  void shouldReturnListOfUsersWhenFindAllUsersWithFilters() throws Exception {
    String expectedJson = JsonUtils.readJson("/users/find-all_with_filters.json");

    String responseJson = mockMvc
        .perform(get("/users").param("page", "0")
            .param("size", "25")
            .param("name", "Carlos Alberto Arroyo Martínez")
            .param("age", "28")
            .param("email", "carroyom@mail.com")
            .param("username", "carroyom")
            .param("roleId", "1")
            .param("isActive", "true"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should return UserDto when find user by id with existing id")
  void shouldReturnUserDtoWhenFindUserByIdWithExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/users/find-by-id.json");

    String responseJson = mockMvc.perform(get("/users/{userId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when find user by id with non existing id")
  void shouldThrowWhenFindUserByIdWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/users/find-by-id_with_non_existing_id.json");

    String responseJson = mockMvc.perform(get("/users/{userId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return created when create a user with valid data")
  void shouldReturnCreatedWhenCreateUserWithValidData() throws Exception {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age((byte) 28)
        .email("carroyom2@mail.com")
        .username("carroyom2")
        .roleId(1)
        .build();

    mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/users/3"));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when create a user with existing username")
  void shouldThrowWhenCreateUserWithExistingUsername() throws Exception {
    String expectedJson = JsonUtils.readJson("/users/create_with_existing_username.json");

    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age((byte) 28)
        .email("carroyom2@mail.com")
        .username("carroyom")
        .roleId(1)
        .build();

    String responseJson = mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when create a user with existing email")
  void shouldThrowWhenCreateUserWithExistingEmail() throws Exception {
    String expectedJson = JsonUtils.readJson("/users/create_with_existing_email.json");

    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age((byte) 28)
        .email("carroyom@mail.com")
        .username("carroyom2")
        .roleId(1)
        .build();

    String responseJson = mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return no content update user with valid data")
  void shouldReturnNoContentWhenUpdateUserWithValidData() throws Exception {
    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age((byte) 30)
        .build();

    mockMvc.perform(put("/users/{userId}", 1L).contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(requestDto))).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when update user with non existing id")
  void shouldThrowWhenUpdateUserWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/users/update_with_non_existing_id.json");

    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age((byte) 28)
        .build();

    String responseJson = mockMvc
        .perform(put("/users/{userId}", 1000L).contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return no content when delete user with existing id")
  void shouldReturnNoContentWhenDeleteUserWithExistingId() throws Exception {
    mockMvc.perform(delete("/users/{userId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete user with non existing id")
  void shouldThrowWhenDeleteUserWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/users/delete_with_non_existing_id.json");

    String responseJson = mockMvc.perform(delete("/users/{userId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }
}
