package com.carlosarroyoam.rest.books.mapper;

import com.carlosarroyoam.rest.books.dto.RoleDto;
import com.carlosarroyoam.rest.books.entity.Role;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {
  RoleDto toDto(Role role);

  List<RoleDto> toDtos(List<Role> roles);
}
