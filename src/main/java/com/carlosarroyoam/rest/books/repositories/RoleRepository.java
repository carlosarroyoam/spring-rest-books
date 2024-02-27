package com.carlosarroyoam.rest.books.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.rest.books.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
