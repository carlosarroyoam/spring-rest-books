package com.carlosarroyoam.rest.books.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.rest.books.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
