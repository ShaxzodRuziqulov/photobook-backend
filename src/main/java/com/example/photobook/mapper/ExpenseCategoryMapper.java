package com.example.photobook.mapper;

import com.example.photobook.dto.ExpenseCategoryDto;
import com.example.photobook.entity.ExpenseCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseCategoryMapper extends EntityMapper<ExpenseCategoryDto, ExpenseCategory>{

}