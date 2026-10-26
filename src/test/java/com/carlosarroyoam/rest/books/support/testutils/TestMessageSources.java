package com.carlosarroyoam.rest.books.support.testutils;

import java.nio.charset.StandardCharsets;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Construye un {@link MessageSource} equivalente al que autoconfigura Spring Boot para usar en
 * {@code MockMvc.standaloneSetup(...)}, donde no hay contexto de Spring Boot que lo configure
 * automaticamente.
 */
public final class TestMessageSources {
  /** Constructor privado que impide instanciar la clase. */
  private TestMessageSources() {
    throw new IllegalAccessError("Illegal access to utility class");
  }

  /**
   * Crea un {@link MessageSource} que lee el bundle {@code messages.properties}, igual que Spring
   * Boot con su basename por defecto.
   *
   * @return el {@link MessageSource} configurado
   */
  public static MessageSource messages() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("messages");
    messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
    return messageSource;
  }
}
