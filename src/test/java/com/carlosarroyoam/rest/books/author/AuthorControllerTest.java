package com.carlosarroyoam.rest.books.author;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.AuthorFilterDto;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
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
class AuthorControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock
  private AuthorService authorService;

  @InjectMocks
  private AuthorController authorController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    mockMvc = MockMvcBuilders.standaloneSetup(authorController)
        .setControllerAdvice(GlobalExceptionHandler.class)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find all authors")
  void shouldReturnListOfAuthorsWhenFindAllAuthors() throws Exception {
    List<AuthorDto> authors = List.of(AuthorDto.builder().build());

    when(authorService.findAll(any(Pageable.class), any(AuthorFilterDto.class)))
        .thenReturn(authors);

    MvcResult mvcResult = mockMvc.perform(get("/authors").queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON)).andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory()
        .constructCollectionType(List.class, AuthorDto.class);
    List<AuthorDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(mvcResult.getResponse().getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    assertThat(responseDto).isNotNull().isNotEmpty().hasSize(1);
  }

  @Test
  @DisplayName("Should return AuthorDto when find author by id with existing id")
  void shouldReturnAuthorDtoWhenFindAuthorById() throws Exception {
    AuthorDto author = AuthorDto.builder().build();

    when(authorService.findById(anyLong())).thenReturn(author);

    MvcResult mvcResult = mockMvc
        .perform(get("/authors/{authorId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AuthorDto responseDto = mapper.readValue(responseJson, AuthorDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(mvcResult.getResponse().getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    assertThat(responseDto).isNotNull();
  }

  @Test
  @DisplayName("Should return created when create an author")
  void shouldReturnCreatedWhenCreateAuthor() throws Exception {
    CreateAuthorRequestDto requestDto = CreateAuthorRequestDto.builder()
        .name("Yuval Noah Harari")
        .build();

    AuthorDto author = AuthorDto.builder().id(1L).build();

    when(authorService.create(any(CreateAuthorRequestDto.class))).thenReturn(author);

    MvcResult mvcResult = mockMvc
        .perform(post("/authors").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(mvcResult.getResponse().getHeader("location"))
        .isEqualTo("http://localhost/authors/1");
  }

  @Test
  @DisplayName("Should return no content when update author")
  void shouldReturnNoContentWhenUpdateAuthor() throws Exception {
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder()
        .name("Yuval Noah Harari")
        .build();

    MvcResult mvcResult = mockMvc
        .perform(put("/authors/{authorId}", 1L).content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should return no content when delete author")
  void shouldReturnNoContentWhenDeleteAuthor() throws Exception {
    MvcResult mvcResult = mockMvc
        .perform(delete("/authors/{authorId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should return List<BookDto> when find books by author id")
  void shouldReturnListOfBooksWhenFindBooksByAuthorId() throws Exception {
    List<BookDto> books = List.of(BookDto.builder().build());

    when(authorService.findBooksByAuthorId(anyLong())).thenReturn(books);

    MvcResult mvcResult = mockMvc
        .perform(get("/authors/{authorId}/books", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory()
        .constructCollectionType(List.class, BookDto.class);
    List<BookDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(mvcResult.getResponse().getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    assertThat(responseDto).isNotNull().isNotEmpty().hasSize(1);
  }
}
