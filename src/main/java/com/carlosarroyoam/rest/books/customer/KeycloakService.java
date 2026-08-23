package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.property.KeycloakAdminProps;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequest;
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

/**
 * Sincroniza los clientes de la API con Keycloak, creando el usuario correspondiente en el realm
 * configurado a través del Admin Client.
 */
@Service
public class KeycloakService {
  private final Keycloak keycloak;
  private final KeycloakAdminProps keycloakAdminProps;

  public KeycloakService(Keycloak keycloak, KeycloakAdminProps keycloakAdminProps) {
    this.keycloak = keycloak;
    this.keycloakAdminProps = keycloakAdminProps;
  }

  /**
   * Crea en Keycloak el usuario asociado a un cliente recién registrado, con el rol {@code
   * App/Customer}. No realiza ninguna acción si ya existe un usuario con el mismo nombre de
   * usuario o correo electrónico.
   *
   * @param request datos del cliente recién creado
   * @param customerId id del cliente en la base de datos de la API, almacenado como atributo del
   *     usuario en Keycloak
   */
  public void createUser(CreateCustomerRequest request, Long customerId) {
    UsersResource usersResource = keycloak.realm(keycloakAdminProps.getRealm()).users();

    List<UserRepresentation> existingUsersByUsername =
        usersResource.searchByUsername(request.getUsername(), true);
    List<UserRepresentation> existingUsersByEmail =
        usersResource.searchByEmail(request.getEmail(), true);

    if (!existingUsersByUsername.isEmpty() || !existingUsersByEmail.isEmpty()) {
      return;
    }

    Map<String, List<String>> attributes = new HashMap<>();
    attributes.put("customerId", List.of(customerId.toString()));

    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setTemporary(false);
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(request.getPassword());

    UserRepresentation user = new UserRepresentation();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setEnabled(true);
    user.setAttributes(attributes);
    user.setCredentials(Collections.singletonList(credential));

    try (Response response = usersResource.create(user)) {
      if (Status.CREATED.getStatusCode() != response.getStatus()) {
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, AppMessages.USER_NOT_CREATED_EXCEPTION);
      }

      String keycloakUserId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

      RoleRepresentation role =
          keycloak
              .realm(keycloakAdminProps.getRealm())
              .roles()
              .get("App/Customer")
              .toRepresentation();

      usersResource.get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(role));
    }
  }
}
