package com.carlosarroyoam.rest.books.user;

import com.carlosarroyoam.rest.books.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
