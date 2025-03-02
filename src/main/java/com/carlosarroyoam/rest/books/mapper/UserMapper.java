package com.carlosarroyoam.rest.books.mapper;

import com.carlosarroyoam.rest.books.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.dto.UserDto;
import com.carlosarroyoam.rest.books.entity.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
  UserDto toDto(User user);

  List<UserDto> toDtos(List<User> users);

  User toEntity(CreateUserRequestDto requestDto);
}
