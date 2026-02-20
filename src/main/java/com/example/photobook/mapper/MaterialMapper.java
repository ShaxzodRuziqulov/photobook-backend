package com.example.photobook.mapper;

import com.example.photobook.dto.MaterialDto;
import com.example.photobook.entity.Material;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MaterialMapper extends EntityMapper<MaterialDto, Material>{

}