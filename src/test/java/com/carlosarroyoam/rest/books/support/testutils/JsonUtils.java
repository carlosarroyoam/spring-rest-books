package com.carlosarroyoam.rest.books.support.testutils;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** Lee fixtures JSON de respuesta esperada desde {@code src/test/resources/responses}. */
public final class JsonUtils {
  private JsonUtils() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  /**
   * Lee un fixture JSON de {@code src/test/resources/responses/<path>} como texto.
   *
   * @param path la ruta del fixture relativa a {@code responses/} (p. ej. {@code
   *     "users/find-by-id.json"})
   * @return el contenido del fixture como cadena UTF-8
   */
  public static String readJson(String path) {
    String resourcePath = "responses/" + path;

    try (InputStream inputStream =
        JsonUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IllegalArgumentException("Fixture not found on classpath: " + resourcePath);
      }

      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new UncheckedIOException("Failed to read fixture: " + resourcePath, ex);
    }
  }
}
