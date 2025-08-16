package com.carlosarroyoam.rest.books.book;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.book.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class BookControllerIT {
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
  @DisplayName("Should return List<BookDto> when find all books")
  void shouldReturnListOfBooksWhenFindAllBooks() throws Exception {
    mockMvc.perform(get("/books").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].isbn").value("978-1-3035-0529-4"))
        .andExpect(jsonPath("$[0].title").value("Homo Deus: A Brief History of Tomorrow"))
        .andExpect(jsonPath("$[0].cover_url")
            .value("https://images.isbndb.com/covers/39/36/9781784703936.jpg"))
        .andExpect(jsonPath("$[0].price").value(22.99))
        .andExpect(jsonPath("$[0].is_available_online").value(false))
        .andExpect(jsonPath("$[0].published_at").value("2017-01-01"));
  }

  @Test
  @DisplayName("Should return BookDto when find book by id with existing id")
  void shouldReturnBookDtoWhenFindBookByIdWithExistingId() throws Exception {
    mockMvc.perform(get("/books/{bookId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.isbn").value("978-1-3035-0529-4"))
        .andExpect(jsonPath("$.title").value("Homo Deus: A Brief History of Tomorrow"))
        .andExpect(jsonPath("$.cover_url")
            .value("https://images.isbndb.com/covers/39/36/9781784703936.jpg"))
        .andExpect(jsonPath("$.price").value(22.99))
        .andExpect(jsonPath("$.is_available_online").value(false))
        .andExpect(jsonPath("$.published_at").value("2017-01-01"));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when find book by id with non existing id")
  void shouldThrowWhenFindBookByIdWithNonExistingId() throws Exception {
    mockMvc.perform(get("/books/{bookId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Book not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Should return when create a book with valid data")
  void shouldReturnCreatedWhenCreateBookWithValidData() throws Exception {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-1-7873-3067-2")
        .title("21 Lessons for the 21st Century")
        .coverUrl("https://images.isbndb.com/covers/9835763482824.jpg")
        .price(new BigDecimal("47.20"))
        .publishedAt(LocalDate.parse("2018-08-30"))
        .isAvailableOnline(true)
        .build();

    mockMvc
        .perform(post("/books").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/books/3"));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when create a book with existing ISBN")
  void shouldThrowWhenCreateBookWithExistingIsbn() throws Exception {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(true)
        .build();

    mockMvc
        .perform(post("/books").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("ISBN already exists"))
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  @DisplayName("Should return no content when update book with valid data")
  void shouldReturnNoContentWhenUpdateBookWithValidData() throws Exception {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(true)
        .build();

    mockMvc.perform(put("/books/{bookId}", 1L).contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(requestDto))).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when update book with non existing id")
  void shouldThrowWhenUpdateBookWithNonExistingId() throws Exception {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(true)
        .build();

    mockMvc
        .perform(put("/books/{bookId}", 1000L).contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Book not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Should return no content when delete book with existing id")
  void shouldReturnNoContentDeleteBookWithExistingId() throws Exception {
    mockMvc.perform(delete("/books/{bookId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete book with non existing id")
  void shouldThrowWhenDeleteBookWithNonExistingId() throws Exception {
    mockMvc.perform(delete("/books/{bookId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Book not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find authors by book id with existing id")
  void shouldReturnListOfAuthorsWhenFindAuthorsByBookIdWithExistingId() throws Exception {
    mockMvc.perform(get("/books/{bookId}/authors", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].name").value("Yuval Noah Harari"));
  }
}
