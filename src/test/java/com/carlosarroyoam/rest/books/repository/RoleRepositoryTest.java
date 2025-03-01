package com.carlosarroyoam.rest.books.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.carlosarroyoam.rest.books.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class RoleRepositoryTest {
  @Autowired
  private RoleRepository roleRepository;

  @Test
  void test() {
    Role adminRole = Role.builder()
        .title("App//Admin")
        .description("Role for admins users")
        .build();

    Role savedRole = roleRepository.save(adminRole);

    assertThat(savedRole).isNotNull();
    assertThat(savedRole.getId()).isNotNull();
    assertThat(savedRole.getDescription()).isEqualTo("App//Admin");
    assertThat(savedRole.getTitle()).isEqualTo("Role for admins users");
  }
}
