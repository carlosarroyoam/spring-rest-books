package com.carlosarroyoam.rest.books.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.carlosarroyoam.rest.books.entity.Role;

@DataJpaTest
class RoleRepositoryTest {

	@Autowired
	private RoleRepository roleRepository;

	@Test
	void test() {
		Role adminRole = new Role("App//Admin", "Role for admins users");

		Role savedRole = roleRepository.save(adminRole);

		assertThat(savedRole).isNotNull();
		assertThat(savedRole.getId()).isNotNull();
		assertThat(savedRole.getTitle()).isEqualTo(adminRole.getTitle());
		assertThat(savedRole.getDescription()).isEqualTo(adminRole.getDescription());
	}

}
