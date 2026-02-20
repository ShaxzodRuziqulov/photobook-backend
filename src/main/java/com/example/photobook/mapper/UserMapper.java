package com.example.photobook.mapper;

import com.example.photobook.dto.UserDto;
import com.example.photobook.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends EntityMapper<UserDto, User> {
}
