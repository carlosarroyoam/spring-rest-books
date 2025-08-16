package com.carlosarroyoam.rest.books.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.exception.ControllerAdvisor;
import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionDto;
import com.carlosarroyoam.rest.books.user.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UpdateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UserDto;
import com.carlosarroyoam.rest.books.user.dto.UserFilterDto;
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
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock
  private UserService userService;

  @InjectMocks
  private UserController userController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    mockMvc = MockMvcBuilders.standaloneSetup(userController)
        .setControllerAdvice(ControllerAdvisor.class)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("Should return List<UserDto> when find all users")
  void shouldReturnListOfUsers() throws Exception {
    List<UserDto> users = List.of(UserDto.builder().build());

    when(userService.findAll(any(Pageable.class), any(UserFilterDto.class))).thenReturn(users);

    MvcResult mvcResult = mockMvc.perform(get("/users").queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON)).andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory()
        .constructCollectionType(List.class, UserDto.class);
    List<UserDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull().isNotEmpty().hasSize(1);
  }

  @Test
  @DisplayName("Should return empty List<UserDto> when find all users")
  void shouldReturnListOfUsersWithEmptyResponse() throws Exception {
    List<UserDto> users = List.of();

    when(userService.findAll(any(Pageable.class), any(UserFilterDto.class))).thenReturn(users);

    MvcResult mvcResult = mockMvc.perform(get("/users").queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON)).andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory()
        .constructCollectionType(List.class, UserDto.class);
    List<UserDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull().isEmpty();
  }

  @Test
  @DisplayName("Should return UserDto when find user by id with existing id")
  void shouldReturnWhenFindUserByIdWithExistingId() throws Exception {
    UserDto user = UserDto.builder().id(1L).build();

    when(userService.findById(anyLong())).thenReturn(user);

    MvcResult mvcResult = mockMvc
        .perform(get("/users/{userId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    UserDto responseDto = mapper.readValue(responseJson, UserDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find user by id with non existing id")
  void shouldReturnWhenFindUserByIdWithNonExistingId() throws Exception {
    when(userService.findById(anyLong())).thenThrow(
        new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND_EXCEPTION));

    MvcResult mvcResult = mockMvc
        .perform(get("/users/{userId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.USER_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("Should return when create a user with valid data")
  void shouldReturnWhenCreateUserWithValidData() throws Exception {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .email("carroyom@mail.com")
        .username("carroyom")
        .roleId(1)
        .build();

    UserDto user = UserDto.builder().id(1L).build();

    when(userService.create(any(CreateUserRequestDto.class))).thenReturn(user);

    MvcResult mvcResult = mockMvc
        .perform(post("/users").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(mvcResult.getResponse().getHeader("location")).isEqualTo("http://localhost/users/1");
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create a user with existing username")
  void shouldThrowWhenCreateUserWithExistingUsername() throws Exception {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .email("carroyom@mail.com")
        .username("carroyom")
        .roleId(1)
        .build();

    when(userService.create(any(CreateUserRequestDto.class))).thenThrow(new ResponseStatusException(
        HttpStatus.BAD_REQUEST, AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION));

    MvcResult mvcResult = mockMvc
        .perform(post("/users").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create a user with existing email")
  void shouldThrowWhenCreateUserWithExistingEmail() throws Exception {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .email("carroyom@mail.com")
        .username("carroyom")
        .roleId(1)
        .build();

    when(userService.create(any(CreateUserRequestDto.class))).thenThrow(new ResponseStatusException(
        HttpStatus.BAD_REQUEST, AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION));

    MvcResult mvcResult = mockMvc
        .perform(post("/users").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("Should update user with valid data")
  void shouldUpdateUserWithValidData() throws Exception {
    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .build();

    MvcResult mvcResult = mockMvc
        .perform(put("/users/{userId}", 1L).content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update user with non existing id")
  void shouldUpdateUserWithNonExistingId() throws Exception {
    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .build();

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND_EXCEPTION))
        .when(userService)
        .update(anyLong(), any(UpdateUserRequestDto.class));

    MvcResult mvcResult = mockMvc
        .perform(put("/users/{userId}", 1L).content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.USER_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("Should delete user with existing id")
  void shouldDeleteUserWithExistingId() throws Exception {
    doNothing().when(userService).deleteById(anyLong());

    MvcResult mvcResult = mockMvc
        .perform(delete("/users/{userId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete user with non existing id")
  void shouldThrowWhenDeleteUserWithNonExistingId() throws Exception {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND_EXCEPTION))
        .when(userService)
        .deleteById(anyLong());

    MvcResult mvcResult = mockMvc
        .perform(delete("/users/{userId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    AppExceptionDto responseDto = mapper.readValue(responseJson, AppExceptionDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getMessage()).isEqualTo(AppMessages.USER_NOT_FOUND_EXCEPTION);
    assertThat(responseDto.getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
    assertThat(responseDto.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }
}
