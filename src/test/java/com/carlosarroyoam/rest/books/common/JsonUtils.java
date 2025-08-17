package com.carlosarroyoam.rest.books.common;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonUtils {
  private JsonUtils() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  public static String readJson(String path) throws IOException {
    return new String(Files.readAllBytes(Paths.get("src/test/resources/responses" + path)));
  }
}
