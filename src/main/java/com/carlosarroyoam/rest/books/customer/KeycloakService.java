package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.property.KeycloakAdminProps;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequestDto;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KeycloakService {
  private final Keycloak keycloak;
  private final KeycloakAdminProps keycloakAdminProps;

  public KeycloakService(Keycloak keycloak, KeycloakAdminProps keycloakAdminProps) {
    this.keycloak = keycloak;
    this.keycloakAdminProps = keycloakAdminProps;
  }

  public void createUser(CreateCustomerRequestDto requestDto, Long customerId) {
    UsersResource usersResource = keycloak.realm(keycloakAdminProps.getRealm()).users();

    List<UserRepresentation> existingUsersByUsername = usersResource
        .searchByUsername(requestDto.getUsername(), true);
    List<UserRepresentation> existingUsersByEmail = usersResource
        .searchByEmail(requestDto.getEmail(), true);

    if (Boolean.FALSE.equals(existingUsersByUsername.isEmpty())
        || Boolean.FALSE.equals(existingUsersByEmail.isEmpty())) {
      return;
    }

    Map<String, List<String>> attributes = new HashMap<>();
    attributes.put("customerId", List.of(customerId.toString()));

    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setTemporary(false);
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(requestDto.getPassword());

    UserRepresentation user = new UserRepresentation();
    user.setFirstName(requestDto.getFirstName());
    user.setLastName(requestDto.getLastName());
    user.setUsername(requestDto.getUsername());
    user.setEmail(requestDto.getEmail());
    user.setEnabled(true);
    user.setAttributes(attributes);
    user.setCredentials(Collections.singletonList(credential));

    try (Response response = usersResource.create(user)) {
      if (Status.CREATED.getStatusCode() != response.getStatus()) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            AppMessages.USER_NOT_CREATED_EXCEPTION);
      }

      String keycloakUserId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

      RoleRepresentation role = keycloak.realm(keycloakAdminProps.getRealm())
          .roles()
          .get("App/Customer")
          .toRepresentation();

      usersResource.get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(role));
    }
  }
}
