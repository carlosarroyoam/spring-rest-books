package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.exception.InternalServerException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Sincroniza los clientes de la API con Keycloak, creando el usuario correspondiente en el realm
 * configurado a través del Admin Client.
 */
@Service
public class KeycloakService {
  private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);
  private final Keycloak keycloak;
  private final KeycloakAdminProps keycloakAdminProps;

  public KeycloakService(Keycloak keycloak, KeycloakAdminProps keycloakAdminProps) {
    this.keycloak = keycloak;
    this.keycloakAdminProps = keycloakAdminProps;
  }

  /**
   * Crea en Keycloak el usuario asociado a un cliente recién registrado, con el rol {@code
   * App/Customer}. Si ya existe un usuario con el mismo nombre de usuario o correo electrónico, se
   * considera un estado inconsistente (el cliente ya fue validado como único en la base de datos
   * local) y se rechaza la operación en lugar de omitirla en silencio.
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
      log.warn(
          "{}: a Keycloak user already exists for username '{}' or email '{}', but customer {}"
              + " passed the local uniqueness check",
          AppMessages.USER_NOT_CREATED_EXCEPTION,
          request.getUsername(),
          request.getEmail(),
          customerId);
      throw new InternalServerException(AppMessages.USER_NOT_CREATED_EXCEPTION);
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
      if (Status.CREATED.getStatusCode() != response.getStatus()
          || response.getLocation() == null) {
        log.warn(
            "{}: Keycloak responded with status {} and Location header {}",
            AppMessages.USER_NOT_CREATED_EXCEPTION,
            response.getStatus(),
            response.getLocation());
        throw new InternalServerException(AppMessages.USER_NOT_CREATED_EXCEPTION);
      }

      String keycloakUserId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

      try {
        RoleRepresentation role =
            keycloak
                .realm(keycloakAdminProps.getRealm())
                .roles()
                .get("App/Customer")
                .toRepresentation();

        usersResource.get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(role));
      } catch (RuntimeException ex) {
        log.warn(
            "{}: rolling back Keycloak user {} created for customer {} after role assignment"
                + " failed",
            AppMessages.USER_NOT_CREATED_EXCEPTION,
            keycloakUserId,
            customerId);
        removeOrphanedUser(usersResource, keycloakUserId);
        throw new InternalServerException(AppMessages.USER_NOT_CREATED_EXCEPTION, ex);
      }
    }
  }

  /**
   * Elimina en Keycloak un usuario que quedó huérfano tras fallar un paso posterior a su creación
   * (p. ej. la asignación de rol), evitando dejarlo sin la sincronización correspondiente en la
   * base de datos local.
   *
   * @param usersResource recurso de usuarios del realm configurado
   * @param keycloakUserId id del usuario a eliminar
   */
  private void removeOrphanedUser(UsersResource usersResource, String keycloakUserId) {
    try {
      usersResource.get(keycloakUserId).remove();
    } catch (RuntimeException cleanupEx) {
      log.warn(
          "{}: failed to roll back orphaned Keycloak user {}, manual cleanup required",
          AppMessages.USER_NOT_CREATED_EXCEPTION,
          keycloakUserId,
          cleanupEx);
    }
  }
}
