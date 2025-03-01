package com.carlosarroyoam.rest.books.mapper;

import com.carlosarroyoam.rest.books.dto.RoleResponse;
import com.carlosarroyoam.rest.books.entity.Role;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {
  RoleResponse toDto(Role role);

  List<RoleResponse> toDtos(List<Role> roles);
}
