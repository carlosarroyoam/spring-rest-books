package com.carlosarroyoam.rest.books.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.carlosarroyoam.rest.books.dtos.RoleResponse;
import com.carlosarroyoam.rest.books.entities.Role;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {

	RoleResponse toDto(Role role);

	List<RoleResponse> toDtos(List<Role> roles);

}
