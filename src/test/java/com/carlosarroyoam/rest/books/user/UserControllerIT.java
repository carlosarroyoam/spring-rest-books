package com.carlosarroyoam.rest.books.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.user.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UpdateUserRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
    mockMvc.perform(get("/users").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].name").value("Carlos Alberto Arroyo Martínez"))
        .andExpect(jsonPath("$[0].age").value("28"))
        .andExpect(jsonPath("$[0].email").value("carroyom@mail.com"))
        .andExpect(jsonPath("$[0].username").value("carroyom"))
        .andExpect(jsonPath("$[0].role.id").value(1))
        .andExpect(jsonPath("$[0].role.title").value("App//Admin"))
        .andExpect(jsonPath("$[0].role.description").value("Role for admins users"))
        .andExpect(jsonPath("$[0].is_active").value(true));
  }

  @Test
  @DisplayName("Should return UserDto when find user by id with existing id")
  void shouldReturnUserDtoWhenFindUserByIdWithExistingId() throws Exception {
    mockMvc.perform(get("/users/{userId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Carlos Alberto Arroyo Martínez"))
        .andExpect(jsonPath("$.age").value("28"))
        .andExpect(jsonPath("$.email").value("carroyom@mail.com"))
        .andExpect(jsonPath("$.username").value("carroyom"))
        .andExpect(jsonPath("$.role.id").value(1))
        .andExpect(jsonPath("$.role.title").value("App//Admin"))
        .andExpect(jsonPath("$.role.description").value("Role for admins users"))
        .andExpect(jsonPath("$.is_active").value(true));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when find user by id with non existing id")
  void shouldThrowWhenFindUserByIdWithNonExistingId() throws Exception {
    mockMvc.perform(get("/users/{userId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("User not found"))
        .andExpect(jsonPath("$.status").value(404));
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
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age((byte) 28)
        .email("carroyom2@mail.com")
        .username("carroyom")
        .roleId(1)
        .build();

    mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Username already exists"))
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when create a user with existing email")
  void shouldThrowWhenCreateUserWithExistingEmail() throws Exception {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age((byte) 28)
        .email("carroyom@mail.com")
        .username("carroyom2")
        .roleId(1)
        .build();

    mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Email already exists"))
        .andExpect(jsonPath("$.status").value(400));
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
    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age((byte) 28)
        .build();

    mockMvc
        .perform(put("/users/{userId}", 1000L).contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("User not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Should return no content when delete user with existing id")
  void shouldReturnNoContentWhenDeleteUserWithExistingId() throws Exception {
    mockMvc.perform(delete("/users/{userId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete user with non existing id")
  void shouldThrowWhenDeleteUserWithNonExistingId() throws Exception {
    mockMvc.perform(delete("/users/{userId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("User not found"))
        .andExpect(jsonPath("$.status").value(404));
  }
}
