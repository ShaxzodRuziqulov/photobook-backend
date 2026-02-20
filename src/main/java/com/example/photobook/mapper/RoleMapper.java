package com.example.photobook.mapper;

import com.example.photobook.dto.RoleDto;
import com.example.photobook.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper extends EntityMapper<RoleDto, Role>{

}