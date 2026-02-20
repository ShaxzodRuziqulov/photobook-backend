package com.example.photobook.mapper;

import com.example.photobook.dto.ProductCategoryDto;
import com.example.photobook.entity.ProductCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper extends EntityMapper<ProductCategoryDto, ProductCategory>{

}