package com.carlosarroyoam.rest.books.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.carlosarroyoam.rest.books.constant.AppMessages;
import com.carlosarroyoam.rest.books.dto.AppExceptionDto;
import com.carlosarroyoam.rest.books.dto.AuthorDto;
import com.carlosarroyoam.rest.books.dto.BookDto;
import com.carlosarroyoam.rest.books.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.dto.UpdateBookRequestDto;
import com.carlosarroyoam.rest.books.exception.ControllerAdvisor;
import com.carlosarroyoam.rest.books.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {
  private MockMvc mockMvc;
  private ObjectMapper mapper;

  @Mock
  private BookService bookService;

  @InjectMocks
  private BookController bookController;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(bookController).setControllerAdvice(ControllerAdvisor.class).build();
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
  }

  @Test
  @DisplayName("Should return List<BookDto> when find all books")
  void shouldReturnListOfBooks() throws Exception {
    List<BookDto> books = List.of(BookDto.builder().build(), BookDto.builder().build());

    Mockito.when(bookService.findAll(any(), any())).thenReturn(books);

    MvcResult mvcResult = mockMvc.perform(get("/books")
        .queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, BookDto.class);
    List<BookDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto).isNotEmpty();
    assertThat(responseDto).size().isEqualTo(2);
  }

  @Test
  @DisplayName("Should return empty List<BookDto> when find all books")
  void shouldReturnListOfBooksWithEmptyResponse() throws Exception {
    List<BookDto> books = List.of();

    Mockito.when(bookService.findAll(any(), any())).thenReturn(books);

    MvcResult mvcResult = mockMvc.perform(get("/books")
        .queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, BookDto.class);
    List<BookDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto).isEmpty();
  }

  @Test
  @DisplayName("Should return BookDto when find book by id with existing id")
  void shouldReturnWhenFindBookByIdWithExistingId() throws Exception {
    BookDto book = BookDto.builder().id(1L).build();

    Mockito.when(bookService.findById(any())).thenReturn(book);

    MvcResult mvcResult = mockMvc.perform(get("/books/{bookId}", 1L)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    BookDto responseDto = mapper.readValue(responseJson, BookDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find book by id with non existing id")
  void shouldReturnWhenFindBookByIdWithNonExistingId() throws Exception {
    Mockito.when(bookService.findById(any()))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
            AppMessages.BOOK_NOT_FOUND_EXCEPTION));

    MvcResult mvcResult = mockMvc.perform(get("/books/{bookId}", 1L)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("Should return when create a book with valid data")
  void shouldReturnWhenCreateBookWithValidData() throws Exception {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    BookDto book = BookDto.builder().id(1L).build();

    Mockito.when(bookService.create(any(CreateBookRequestDto.class))).thenReturn(book);

    MvcResult mvcResult = mockMvc.perform(post("/books")
        .content(mapper.writeValueAsString(requestDto))
        .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(mvcResult.getResponse().getHeader("location")).isEqualTo("http://localhost/books/1");
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create a book with existing ISBN")
  void shouldThrowWhenCreateBookWithExistingIsbn() throws Exception {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    Mockito.when(bookService.create(any(CreateBookRequestDto.class)))
        .thenThrow(new ResponseStatusException(
            HttpStatus.BAD_REQUEST, AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION));

    MvcResult mvcResult = mockMvc.perform(post("/books")
        .content(mapper.writeValueAsString(requestDto))
        .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("Should update book with valid data")
  void shouldUpdateBookWithValidData() throws Exception {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    MvcResult mvcResult = mockMvc.perform(put("/books/{bookId}", 1L)
        .content(mapper.writeValueAsString(requestDto))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update book with non existing id")
  void shouldUpdateBookWithNonExistingId() throws Exception {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION))
        .when(bookService)
        .update(any(), any(UpdateBookRequestDto.class));

    MvcResult mvcResult = mockMvc.perform(put("/books/{bookId}", 1L)
        .content(mapper.writeValueAsString(requestDto))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("Should delete book with existing id")
  void shouldDeleteBookWithExistingId() throws Exception {
    Mockito.doNothing().when(bookService).deleteById(any());

    MvcResult mvcResult = mockMvc.perform(delete("/books/{bookId}", 1L)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete book with non existing id")
  void shouldThrowWhenDeleteBookWithNonExistingId() throws Exception {
    Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION))
        .when(bookService)
        .deleteById(any());

    MvcResult mvcResult = mockMvc.perform(delete("/books/{bookId}", 1L)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find authors by book id with existing id")
  void shouldReturnWhenFindAuthorsByBookIdWithExistingId() throws Exception {
    List<AuthorDto> authors = List.of(AuthorDto.builder().build(), AuthorDto.builder().build());

    Mockito.when(bookService.findAuthorsByBookId(any())).thenReturn(authors);

    MvcResult mvcResult = mockMvc.perform(get("/books/{bookId}/authors", 1L)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, AuthorDto.class);
    List<AuthorDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto).isNotEmpty();
    assertThat(responseDto).size().isEqualTo(2);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find authors by book id with non existing id")
  void shouldThrowWhenFindAuthorsByBookIdWithNonExistingId() throws Exception {
    Mockito.when(bookService.findAuthorsByBookId(any()))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
            AppMessages.BOOK_NOT_FOUND_EXCEPTION));

    MvcResult mvcResult = mockMvc.perform(get("/books/{bookId}/authors", 1L)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }
}
