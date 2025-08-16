package com.carlosarroyoam.rest.books.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.book.dto.BookFilterDto;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequestDto;
import com.carlosarroyoam.rest.books.core.exception.ControllerAdvisor;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock
  private BookService bookService;

  @InjectMocks
  private BookController bookController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    mockMvc = MockMvcBuilders.standaloneSetup(bookController)
        .setControllerAdvice(ControllerAdvisor.class)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("Should return List<BookDto> when find all books")
  void shouldReturnListOfBooksWhenFindAllBooks() throws Exception {
    List<BookDto> books = List.of(BookDto.builder().build());

    when(bookService.findAll(any(Pageable.class), any(BookFilterDto.class))).thenReturn(books);

    MvcResult mvcResult = mockMvc.perform(get("/books").queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON)).andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory()
        .constructCollectionType(List.class, BookDto.class);
    List<BookDto> responseDto = mapper.readValue(responseJson, collectionType);
    System.out.println("Response JSON: " + responseJson);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull().isNotEmpty().hasSize(1);
  }

  @Test
  @DisplayName("Should return BookDto when find book by id")
  void shouldReturnBookDtoWhenFindBookById() throws Exception {
    BookDto book = BookDto.builder().build();

    when(bookService.findById(anyLong())).thenReturn(book);

    MvcResult mvcResult = mockMvc
        .perform(get("/books/{bookId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    BookDto responseDto = mapper.readValue(responseJson, BookDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull();
  }

  @Test
  @DisplayName("Should return created when create a book")
  void shouldReturnCreatedWhenCreateBook() throws Exception {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    BookDto book = BookDto.builder().id(1L).build();

    when(bookService.create(any(CreateBookRequestDto.class))).thenReturn(book);

    MvcResult mvcResult = mockMvc
        .perform(post("/books").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(mvcResult.getResponse().getHeader("location")).isEqualTo("http://localhost/books/1");
  }

  @Test
  @DisplayName("Should return no content when update book")
  void shouldReturnNoContentWhenUpdateBook() throws Exception {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    MvcResult mvcResult = mockMvc
        .perform(put("/books/{bookId}", 1L).content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should return no content when delete book")
  void shouldReturnNoContentWhenDeleteBook() throws Exception {
    MvcResult mvcResult = mockMvc
        .perform(delete("/books/{bookId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find authors by book id")
  void shouldReturnListOfAuthorsWhenFindAuthorsByBookId() throws Exception {
    List<AuthorDto> authors = List.of(AuthorDto.builder().build());

    when(bookService.findAuthorsByBookId(anyLong())).thenReturn(authors);

    MvcResult mvcResult = mockMvc
        .perform(get("/books/{bookId}/authors", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory()
        .constructCollectionType(List.class, AuthorDto.class);
    List<AuthorDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull().isNotEmpty().hasSize(1);
  }
}
