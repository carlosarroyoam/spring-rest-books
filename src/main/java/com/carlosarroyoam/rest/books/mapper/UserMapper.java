package com.carlosarroyoam.rest.books.mapper;

import com.carlosarroyoam.rest.books.dto.CreateUserRequest;
import com.carlosarroyoam.rest.books.dto.UserResponse;
import com.carlosarroyoam.rest.books.entity.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
  UserResponse toDto(User user);

  List<UserResponse> toDtos(List<User> users);

  User toEntity(CreateUserRequest createUserRequest);
}
