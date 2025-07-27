package com.carlosarroyoam.rest.books.controller;

import com.carlosarroyoam.rest.books.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.dto.UpdateBookRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class BookControllerIT {
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
  @DisplayName("Should return List<BookDto> when find all books")
  void shouldReturnListOfBooks() {
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path("/books")
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
        .jsonPath("$[0].isbn")
        .isEqualTo("978-1-3035-0529-4")
        .jsonPath("$[0].title")
        .isEqualTo("Homo Deus: A Brief History of Tomorrow")
        .jsonPath("$[0].cover_url")
        .isEqualTo("https://images.isbndb.com/covers/39/36/9781784703936.jpg")
        .jsonPath("$[0].price")
        .isEqualTo(new BigDecimal("22.99"))
        .jsonPath("$[0].is_available_online")
        .isEqualTo(Boolean.FALSE)
        .jsonPath("$[0].published_at")
        .isEqualTo(LocalDate.parse("2017-01-01"))
        .jsonPath("$[1].id")
        .isEqualTo(2L)
        .jsonPath("$[1].isbn")
        .isEqualTo("978-9-7389-4434-3")
        .jsonPath("$[1].title")
        .isEqualTo("Sapiens: A Brief History of Humankind")
        .jsonPath("$[1].cover_url")
        .isEqualTo("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .jsonPath("$[1].price")
        .isEqualTo(new BigDecimal("20.79"))
        .jsonPath("$[1].is_available_online")
        .isEqualTo(Boolean.FALSE)
        .jsonPath("$[1].published_at")
        .isEqualTo(LocalDate.parse("2022-12-01"));
  }

  @Test
  @DisplayName("Should return BookDto when find book by id with existing id")
  void shouldReturnWhenFindBookByIdWithExistingId() {
    webTestClient.get()
        .uri("/books/{bookId}", 1L)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.id")
        .isEqualTo(1L)
        .jsonPath("$.isbn")
        .isEqualTo("978-1-3035-0529-4")
        .jsonPath("$.title")
        .isEqualTo("Homo Deus: A Brief History of Tomorrow")
        .jsonPath("$.cover_url")
        .isEqualTo("https://images.isbndb.com/covers/39/36/9781784703936.jpg")
        .jsonPath("$.price")
        .isEqualTo(new BigDecimal("22.99"))
        .jsonPath("$.is_available_online")
        .isEqualTo(Boolean.FALSE)
        .jsonPath("$.published_at")
        .isEqualTo(LocalDate.parse("2017-01-01"));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when find book by id with non existing id")
  void shouldReturnWhenFindBookByIdWithNonExistingId() {
    webTestClient.get()
        .uri("/books/{bookId}", 1000L)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error")
        .isEqualTo("Not Found")
        .jsonPath("$.message")
        .isEqualTo("Book not found")
        .jsonPath("$.status")
        .isEqualTo(404);
  }

  @Test
  @DisplayName("Should return when create a book with valid data")
  void shouldReturnWhenCreateBookWithValidData() {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-1-7873-3067-2")
        .title("21 Lessons for the 21st Century")
        .coverUrl("https://images.isbndb.com/covers/9835763482824.jpg")
        .price(new BigDecimal("47.20"))
        .publishedAt(LocalDate.parse("2018-08-30"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    webTestClient.post()
        .uri("/books")
        .body(Mono.just(requestDto), CreateBookRequestDto.class)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectHeader()
        .location("http://localhost/books/3");
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when create a book with existing ISBN")
  void shouldThrowWhenCreateBookWithExistingIsbn() {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    webTestClient.post()
        .uri("/books")
        .body(Mono.just(requestDto), CreateBookRequestDto.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error")
        .isEqualTo("Bad Request")
        .jsonPath("$.message")
        .isEqualTo("ISBN already exists")
        .jsonPath("$.status")
        .isEqualTo(400);
  }

  @Test
  @DisplayName("Should update book with valid data")
  void shouldUpdateBookWithValidData() {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    webTestClient.put()
        .uri("/books/{bookId}", 1L)
        .body(Mono.just(requestDto), UpdateBookRequestDto.class)
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update book with non existing id")
  void shouldUpdateBookWithNonExistingId() {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    webTestClient.put()
        .uri("/books/{bookId}", 1000L)
        .body(Mono.just(requestDto), UpdateBookRequestDto.class)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error")
        .isEqualTo("Not Found")
        .jsonPath("$.message")
        .isEqualTo("Book not found")
        .jsonPath("$.status")
        .isEqualTo(404);
  }

  @Test
  @DisplayName("Should delete book with existing id")
  void shouldDeleteBookWithExistingId() {
    webTestClient.delete().uri("/books/{bookId}", 1L).exchange().expectStatus().isNoContent();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete book with non existing id")
  void shouldThrowWhenDeleteBookWithNonExistingId() {
    webTestClient.delete()
        .uri("/books/{bookId}", 1000L)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody()
        .jsonPath("$.error")
        .isEqualTo("Not Found")
        .jsonPath("$.message")
        .isEqualTo("Book not found")
        .jsonPath("$.status")
        .isEqualTo(404);
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find authors by book id with existing id")
  void shouldReturnWhenFindAuthorsByBookIdWithExistingId() {
    webTestClient.get()
        .uri("/books/{bookId}/authors", 1L)
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
        .isEqualTo("Yuval Noah Harari")
        .jsonPath("$[1].id")
        .isEqualTo(2L)
        .jsonPath("$[1].name")
        .isEqualTo("Itzik Yahav");
  }
}
