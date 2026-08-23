package com.carlosarroyoam.rest.books.customer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.property.KeycloakAdminProps;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequest;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {
  @Mock private Keycloak keycloak;

  @Mock private KeycloakAdminProps keycloakAdminProps;

  @Mock private RealmResource realmResource;

  @Mock private UsersResource usersResource;

  @InjectMocks private KeycloakService keycloakService;

  private CreateCustomerRequest request;

  @BeforeEach
  void setUp() {
    request =
        CreateCustomerRequest.builder()
            .firstName("Cathy Stefania")
            .lastName("Guido Rojas")
            .email("cguidor@mail.com")
            .username("cguidor")
            .password("secret123#")
            .build();

    when(keycloakAdminProps.getRealm()).thenReturn("rest-books");
    when(keycloak.realm("rest-books")).thenReturn(realmResource);
    when(realmResource.users()).thenReturn(usersResource);
  }

  @Test
  @DisplayName(
      "Given a Keycloak user already exists by username, when create user, then throws internal"
          + " server error exception and does not create the user")
  void givenExistingKeycloakUserByUsername_whenCreateUser_thenThrowsExceptionAndDoesNotCreateUser() {
    when(usersResource.searchByUsername(anyString(), anyBoolean()))
        .thenReturn(List.of(new UserRepresentation()));
    when(usersResource.searchByEmail(anyString(), anyBoolean())).thenReturn(List.of());

    assertThatThrownBy(() -> keycloakService.createUser(request, 1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.INTERNAL_SERVER_ERROR.toString())
        .hasMessageContaining(AppMessages.USER_NOT_CREATED_EXCEPTION);

    verify(usersResource, never()).create(any());
  }

  @Test
  @DisplayName(
      "Given a Keycloak user already exists by email, when create user, then throws internal"
          + " server error exception and does not create the user")
  void givenExistingKeycloakUserByEmail_whenCreateUser_thenThrowsExceptionAndDoesNotCreateUser() {
    when(usersResource.searchByUsername(anyString(), anyBoolean())).thenReturn(List.of());
    when(usersResource.searchByEmail(anyString(), anyBoolean()))
        .thenReturn(List.of(new UserRepresentation()));

    assertThatThrownBy(() -> keycloakService.createUser(request, 1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.INTERNAL_SERVER_ERROR.toString())
        .hasMessageContaining(AppMessages.USER_NOT_CREATED_EXCEPTION);

    verify(usersResource, never()).create(any());
  }

  @Test
  @DisplayName(
      "Given Keycloak creates the user without a Location header, when create user, then throws"
          + " internal server error exception")
  void givenCreatedResponseWithoutLocationHeader_whenCreateUser_thenThrowsException() {
    Response response = mock(Response.class);
    when(response.getStatus()).thenReturn(Status.CREATED.getStatusCode());
    when(response.getLocation()).thenReturn(null);

    when(usersResource.searchByUsername(anyString(), anyBoolean())).thenReturn(List.of());
    when(usersResource.searchByEmail(anyString(), anyBoolean())).thenReturn(List.of());
    when(usersResource.create(any())).thenReturn(response);

    assertThatThrownBy(() -> keycloakService.createUser(request, 1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.INTERNAL_SERVER_ERROR.toString())
        .hasMessageContaining(AppMessages.USER_NOT_CREATED_EXCEPTION);
  }

  @Test
  @DisplayName(
      "Given role assignment fails after the Keycloak user is created, when create user, then"
          + " removes the created user and throws internal server error exception")
  void givenRoleAssignmentFails_whenCreateUser_thenRemovesCreatedUserAndThrowsException() {
    Response response = mock(Response.class);
    when(response.getStatus()).thenReturn(Status.CREATED.getStatusCode());
    when(response.getLocation())
        .thenReturn(URI.create("http://keycloak/admin/realms/rest-books/users/abc-123"));

    when(usersResource.searchByUsername(anyString(), anyBoolean())).thenReturn(List.of());
    when(usersResource.searchByEmail(anyString(), anyBoolean())).thenReturn(List.of());
    when(usersResource.create(any())).thenReturn(response);

    RolesResource rolesResource = mock(RolesResource.class);
    RoleResource roleResource = mock(RoleResource.class);
    when(realmResource.roles()).thenReturn(rolesResource);
    when(rolesResource.get("App/Customer")).thenReturn(roleResource);
    when(roleResource.toRepresentation()).thenThrow(new NotFoundException("Role not found"));

    UserResource userResource = mock(UserResource.class);
    when(usersResource.get("abc-123")).thenReturn(userResource);

    assertThatThrownBy(() -> keycloakService.createUser(request, 1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.INTERNAL_SERVER_ERROR.toString())
        .hasMessageContaining(AppMessages.USER_NOT_CREATED_EXCEPTION);

    verify(userResource).remove();
  }
}
