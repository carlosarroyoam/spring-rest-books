package com.carlosarroyoam.rest.books.book;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.book.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequestDto;
import com.carlosarroyoam.rest.books.common.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    String expectedJson = JsonUtils.readJson("/books/find-all.json");

    String responseJson = mockMvc.perform(get("/books").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should return BookDto when find book by id with existing id")
  void shouldReturnBookDtoWhenFindBookByIdWithExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/books/find-by-id.json");

    String responseJson = mockMvc.perform(get("/books/{bookId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when find book by id with non existing id")
  void shouldThrowWhenFindBookByIdWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/books/find-by-id_with_non_existing_id.json");

    String responseJson = mockMvc.perform(get("/books/{bookId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
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
    String expectedJson = JsonUtils.readJson("/books/create_with_existing_isbn.json");

    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(true)
        .build();

    String responseJson = mockMvc
        .perform(post("/books").contentType(MediaType.APPLICATION_JSON)
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
    String expectedJson = JsonUtils.readJson("/books/update_with_non_existing_id.json");

    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(true)
        .build();

    String responseJson = mockMvc
        .perform(put("/books/{bookId}", 1000L).contentType(MediaType.APPLICATION_JSON)
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
  @DisplayName("Should return no content when delete book with existing id")
  void shouldReturnNoContentDeleteBookWithExistingId() throws Exception {
    mockMvc.perform(delete("/books/{bookId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete book with non existing id")
  void shouldThrowWhenDeleteBookWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/books/delete_with_non_existing_id.json");

    String responseJson = mockMvc.perform(delete("/books/{bookId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find authors by book id with existing id")
  void shouldReturnListOfAuthorsWhenFindAuthorsByBookIdWithExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/books/find-authors-by-book.json");

    String responseJson = mockMvc.perform(get("/books/{bookId}/authors", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }
}
