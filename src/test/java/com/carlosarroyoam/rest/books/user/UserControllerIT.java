package com.carlosarroyoam.rest.books.user;

import com.carlosarroyoam.rest.books.user.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UpdateUserRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@WithMockUser
@Transactional
class UserControllerIT {
  private WebTestClient webTestClient;

  @Autowired
  public void setWebApplicationContext(final WebApplicationContext context) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    mapper.findAndRegisterModules();

    ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder().codecs(configurer -> {
      configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
      configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
    }).build();

    webTestClient = MockMvcWebTestClient.bindToApplicationContext(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .configureClient()
        .exchangeStrategies(exchangeStrategies)
        .build();
  }

  @Test
  @DisplayName("Should return List<UserDto> when find all users")
  void shouldReturnListOfUsers() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path("/users")
            .queryParam("page", "0")
            .queryParam("size", "25")
            .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(2)
        .jsonPath("$[0].id")
        .isEqualTo(1L)
        .jsonPath("$[0].name")
        .isEqualTo("Carlos Alberto Arroyo Martínez")
        .jsonPath("$[0].age")
        .isEqualTo("28")
        .jsonPath("$[0].email")
        .isEqualTo("carroyom@mail.com")
        .jsonPath("$[0].username")
        .isEqualTo("carroyom")
        .jsonPath("$[0].role_id")
        .isEqualTo(1)
        .jsonPath("$[0].is_active")
        .isEqualTo(Boolean.TRUE)
        .jsonPath("$[1].id")
        .isEqualTo(2L)
        .jsonPath("$[1].name")
        .isEqualTo("Cathy Stefania Guido Rojas")
        .jsonPath("$[1].age")
        .isEqualTo("28")
        .jsonPath("$[1].email")
        .isEqualTo("cguidor@mail.com")
        .jsonPath("$[1].username")
        .isEqualTo("cguidor")
        .jsonPath("$[1].role_id")
        .isEqualTo(2)
        .jsonPath("$[1].is_active")
        .isEqualTo(Boolean.TRUE);
  }

  @Test
  @DisplayName("Should return UserDto when find user by id with existing id")
  void shouldReturnWhenFindUserByIdWithExistingId() {
    webTestClient.get()
        .uri("/users/{userId}", 1L)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.id")
        .isEqualTo(1L)
        .jsonPath("$.name")
        .isEqualTo("Carlos Alberto Arroyo Martínez")
        .jsonPath("$.age")
        .isEqualTo("28")
        .jsonPath("$.email")
        .isEqualTo("carroyom@mail.com")
        .jsonPath("$.username")
        .isEqualTo("carroyom")
        .jsonPath("$.role_id")
        .isEqualTo(1)
        .jsonPath("$.is_active")
        .isEqualTo(Boolean.TRUE);
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when find user by id with non existing id")
  void shouldReturnWhenFindUserByIdWithNonExistingId() {
    webTestClient.get()
        .uri("/users/{userId}", 1000L)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error")
        .isEqualTo("Not Found")
        .jsonPath("$.message")
        .isEqualTo("User not found")
        .jsonPath("$.status")
        .isEqualTo(404);
  }

  @Test
  @DisplayName("Should return when create a user with valid data")
  void shouldReturnWhenCreateUserWithValidData() throws Exception {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .email("carroyom2@mail.com")
        .username("carroyom2")
        .roleId(1)
        .build();

    webTestClient.post()
        .uri("/users")
        .body(Mono.just(requestDto), CreateUserRequestDto.class)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectHeader()
        .location("http://localhost/users/3");
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when create a user with existing username")
  void shouldThrowWhenCreateUserWithExistingUsername() throws Exception {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .email("carroyom2@mail.com")
        .username("carroyom")
        .roleId(1)
        .build();

    webTestClient.post()
        .uri("/users")
        .body(Mono.just(requestDto), CreateUserRequestDto.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error")
        .isEqualTo("Bad Request")
        .jsonPath("$.message")
        .isEqualTo("Username already exists")
        .jsonPath("$.status")
        .isEqualTo(400);
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when create a user with existing email")
  void shouldThrowWhenCreateUserWithExistingEmail() throws Exception {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .email("carroyom@mail.com")
        .username("carroyom2")
        .roleId(1)
        .build();

    webTestClient.post()
        .uri("/users")
        .body(Mono.just(requestDto), CreateUserRequestDto.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error")
        .isEqualTo("Bad Request")
        .jsonPath("$.message")
        .isEqualTo("Email already exists")
        .jsonPath("$.status")
        .isEqualTo(400);
  }

  @Test
  @DisplayName("Should update user with valid data")
  void shouldUpdateUserWithValidData() throws Exception {
    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("30"))
        .build();

    webTestClient.put()
        .uri("/users/{userId}", 1L)
        .body(Mono.just(requestDto), UpdateUserRequestDto.class)
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when update user with non existing id")
  void shouldUpdateUserWithNonExistingId() throws Exception {
    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .build();

    webTestClient.put()
        .uri("/users/{userId}", 1000L)
        .body(Mono.just(requestDto), UpdateUserRequestDto.class)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error")
        .isEqualTo("Not Found")
        .jsonPath("$.message")
        .isEqualTo("User not found")
        .jsonPath("$.status")
        .isEqualTo(404);
  }

  @Test
  @DisplayName("Should delete user with existing id")
  void shouldDeleteUserWithExistingId() {
    webTestClient.delete().uri("/users/{userId}", 1L).exchange().expectStatus().isNoContent();
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete user with non existing id")
  void shouldThrowWhenDeleteUserWithNonExistingId() {
    webTestClient.delete()
        .uri("/users/{userId}", 1000L)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error")
        .isEqualTo("Not Found")
        .jsonPath("$.message")
        .isEqualTo("User not found")
        .jsonPath("$.status")
        .isEqualTo(404);
  }
}
