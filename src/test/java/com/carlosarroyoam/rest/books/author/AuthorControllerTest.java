package com.carlosarroyoam.rest.books.author;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.exception.ControllerAdvisor;
import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
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
class AuthorControllerTest {
  private MockMvc mockMvc;
  private ObjectMapper mapper;

  @Mock
  private AuthorService authorService;

  @InjectMocks
  private AuthorController authorController;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(authorController)
        .setControllerAdvice(ControllerAdvisor.class)
        .build();
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find all authors")
  void shouldReturnListOfAuthors() throws Exception {
    List<AuthorDto> authors = List.of(AuthorDto.builder().build(), AuthorDto.builder().build());

    Mockito.when(authorService.findAll(any(), any())).thenReturn(authors);

    MvcResult mvcResult = mockMvc.perform(get("/authors").queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON)).andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory()
        .constructCollectionType(List.class, AuthorDto.class);
    List<AuthorDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull().isNotEmpty().size().isEqualTo(2);
  }

  @Test
  @DisplayName("Should return empty List<AuthorDto> when find all authors")
  void shouldReturnListOfAuthorsWithEmptyResponse() throws Exception {
    List<AuthorDto> authors = List.of();

    Mockito.when(authorService.findAll(any(), any())).thenReturn(authors);

    MvcResult mvcResult = mockMvc.perform(get("/authors").queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON)).andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory()
        .constructCollectionType(List.class, AuthorDto.class);
    List<AuthorDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull().isEmpty();
  }

  @Test
  @DisplayName("Should return AuthorDto when find author by id with existing id")
  void shouldReturnWhenFindAuthorByIdWithExistingId() throws Exception {
    AuthorDto author = AuthorDto.builder().id(1L).build();

    Mockito.when(authorService.findById(any())).thenReturn(author);

    MvcResult mvcResult = mockMvc
        .perform(get("/authors/{authorId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AuthorDto responseDto = mapper.readValue(responseJson, AuthorDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find author by id with non existing id")
  void shouldReturnWhenFindAuthorByIdWithNonExistingId() throws Exception {
    Mockito.when(authorService.findById(any()))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
            AppMessages.AUTHOR_NOT_FOUND_EXCEPTION));

    MvcResult mvcResult = mockMvc
        .perform(get("/authors/{authorId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("Should return when create an author with valid data")
  void shouldReturnWhenCreateAuthorWithValidData() throws Exception {
    CreateAuthorRequestDto requestDto = CreateAuthorRequestDto.builder()
        .name("Yuval Noah Harari")
        .build();

    AuthorDto author = AuthorDto.builder().id(1L).build();

    Mockito.when(authorService.create(any(CreateAuthorRequestDto.class))).thenReturn(author);

    MvcResult mvcResult = mockMvc
        .perform(post("/authors").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(mvcResult.getResponse().getHeader("location"))
        .isEqualTo("http://localhost/authors/1");
  }

  @Test
  @DisplayName("Should update author with valid data")
  void shouldUpdateAuthorWithValidData() throws Exception {
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
  @DisplayName("Should throw ResponseStatusException when update author with non existing id")
  void shouldUpdateAuthorWithNonExistingId() throws Exception {
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder()
        .name("Yuval Noah Harari")
        .build();

    Mockito
        .doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
            AppMessages.AUTHOR_NOT_FOUND_EXCEPTION))
        .when(authorService)
        .update(any(), any(UpdateAuthorRequestDto.class));

    MvcResult mvcResult = mockMvc
        .perform(put("/authors/{authorId}", 1L).content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("Should delete author with existing id")
  void shouldDeleteAuthorWithExistingId() throws Exception {
    Mockito.doNothing().when(authorService).deleteById(any());

    MvcResult mvcResult = mockMvc
        .perform(delete("/authors/{authorId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete author with non existing id")
  void shouldThrowWhenDeleteAuthorWithNonExistingId() throws Exception {
    Mockito
        .doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
            AppMessages.AUTHOR_NOT_FOUND_EXCEPTION))
        .when(authorService)
        .deleteById(any());

    MvcResult mvcResult = mockMvc
        .perform(delete("/authors/{authorId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("Should return List<BookDto> when find books by author id with existing id")
  void shouldReturnWhenFindBooksByAuthorIdWithExistingId() throws Exception {
    List<BookDto> books = List.of(BookDto.builder().build(), BookDto.builder().build());

    Mockito.when(authorService.findBooksByAuthorId(any())).thenReturn(books);

    MvcResult mvcResult = mockMvc
        .perform(get("/authors/{authorId}/books", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory()
        .constructCollectionType(List.class, BookDto.class);
    List<BookDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull().isNotEmpty().size().isEqualTo(2);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find books by author id with non existing id")
  void shouldThrowWhenFindBooksByAuthorIdWithNonExistingId() throws Exception {
    Mockito.when(authorService.findBooksByAuthorId(any()))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
            AppMessages.AUTHOR_NOT_FOUND_EXCEPTION));

    MvcResult mvcResult = mockMvc
        .perform(get("/authors/{authorId}/books", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }
}
