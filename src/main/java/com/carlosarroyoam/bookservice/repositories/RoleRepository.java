package com.carlosarroyoam.bookservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.bookservice.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
