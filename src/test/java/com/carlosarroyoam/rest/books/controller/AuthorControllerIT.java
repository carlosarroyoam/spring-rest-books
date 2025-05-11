package com.carlosarroyoam.rest.books.controller;

import com.carlosarroyoam.rest.books.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.dto.UpdateAuthorRequestDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@WithMockUser
@Transactional
class AuthorControllerIT {
  private WebTestClient webTestClient;

  @Autowired
  public void setWebApplicationContext(final WebApplicationContext context) {
    webTestClient = MockMvcWebTestClient.bindToApplicationContext(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();
  }

  @Test
  @DisplayName("Should return authors when find all authors")
  void shouldReturnListOfAuthors() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/authors")
            .queryParam("page", "0")
            .queryParam("size", "25")
            .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("$[0].id").isEqualTo(1L)
        .jsonPath("$[0].name").isEqualTo("Yuval Noah Harari")
        .jsonPath("$[1].id").isEqualTo(2L)
        .jsonPath("$[1].name").isEqualTo("Itzik Yahav");
  }

  @Test
  @DisplayName("Should return AuthorDto when find author by id with existing id")
  void shouldReturnWhenFindAuthorByIdWithExistingId() {
    webTestClient
        .get()
        .uri("/authors/{authorId}", 1L)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.id").isEqualTo(1L)
        .jsonPath("$.name").isEqualTo("Yuval Noah Harari");
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when find author by id with non existing id")
  void shouldReturnWhenFindAuthorByIdWithNonExistingId() {
    webTestClient
        .get()
        .uri("/authors/{authorId}", 1000L)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error").isEqualTo("Not Found")
        .jsonPath("$.message").isEqualTo("Author not found")
        .jsonPath("$.status").isEqualTo(404);
  }

  @Test
  @DisplayName("Should return when create an author with valid data")
  void shouldReturnWhenCreateAuthorWithValidData() {
    CreateAuthorRequestDto requestDto = CreateAuthorRequestDto.builder().name("Yuval Noah Harari").build();

    webTestClient
        .post()
        .uri("/authors")
        .body(Mono.just(requestDto), CreateAuthorRequestDto.class)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectHeader()
        .location("http://localhost/authors/3");
  }

  @Test
  @DisplayName("Should update author with valid data")
  void shouldUpdateAuthorWithValidData() {
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder().name("Yuval Noah").build();

    webTestClient
        .put()
        .uri("/authors/{authorId}", 1L)
        .body(Mono.just(requestDto), CreateAuthorRequestDto.class)
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when update author with non existing id")
  void shouldUpdateAuthorWithNonExistingId() {
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder().name("Yuval Noah").build();

    webTestClient
        .put()
        .uri("/authors/{authorId}", 1000L)
        .body(Mono.just(requestDto), CreateAuthorRequestDto.class)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error").isEqualTo("Not Found")
        .jsonPath("$.message").isEqualTo("Author not found")
        .jsonPath("$.status").isEqualTo(404);
  }

  @Test
  @DisplayName("Should delete author with existing id")
  void shouldDeleteAuthorWithExistingId() {
    webTestClient
        .delete()
        .uri("/authors/{authorId}", 1L)
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete author with non existing id")
  void shouldThrowWhenDeleteAuthorWithNonExistingId() {
    webTestClient
        .delete()
        .uri("/authors/{authorId}", 1000L)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error").isEqualTo("Not Found")
        .jsonPath("$.message").isEqualTo("Author not found")
        .jsonPath("$.status").isEqualTo(404);
  }

  @Test
  @DisplayName("Should return List<BookDto> when find books by author id with existing id")
  void shouldReturnWhenFindBooksByAuthorIdWithExistingId() throws Exception {
    webTestClient
        .get()
        .uri("/authors/{authorId}/books", 1L)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("$[0].id").isEqualTo(1L)
        .jsonPath("$[0].isbn").isEqualTo("978-1-3035-0529-4")
        .jsonPath("$[0].title").isEqualTo("Homo Deus: A Brief History of Tomorrow")
        .jsonPath("$[0].cover_url").isEqualTo("https://images.isbndb.com/covers/39/36/9781784703936.jpg")
        .jsonPath("$[0].price").isEqualTo(new BigDecimal("22.99"))
        .jsonPath("$[0].is_available_online").isEqualTo(Boolean.FALSE)
        .jsonPath("$[0].published_at").isEqualTo(LocalDate.parse("2017-01-01"))
        .jsonPath("$[1].id").isEqualTo(2L)
        .jsonPath("$[1].isbn").isEqualTo("978-9-7389-4434-3")
        .jsonPath("$[1].title").isEqualTo("Sapiens: A Brief History of Humankind")
        .jsonPath("$[1].cover_url").isEqualTo("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .jsonPath("$[1].price").isEqualTo(new BigDecimal("20.79"))
        .jsonPath("$[1].is_available_online").isEqualTo(Boolean.FALSE)
        .jsonPath("$[1].published_at").isEqualTo(LocalDate.parse("2022-12-01"));
  }
}
