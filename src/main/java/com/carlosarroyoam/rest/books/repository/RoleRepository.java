package com.carlosarroyoam.rest.books.repository;

import com.carlosarroyoam.rest.books.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
