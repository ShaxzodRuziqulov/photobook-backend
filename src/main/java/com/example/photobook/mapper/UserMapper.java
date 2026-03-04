package com.example.photobook.mapper;

import com.example.photobook.dto.UserDto;
import com.example.photobook.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper extends EntityMapper<UserDto, User> {

    @Override
    @Mapping(target = "password", source = "password")
    User toEntity(UserDto dto);

    @Override
    @Mapping(target = "password", ignore = true)
    UserDto toDto(User entity);
}
