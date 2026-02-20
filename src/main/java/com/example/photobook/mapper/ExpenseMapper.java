package com.example.photobook.mapper;

import com.example.photobook.dto.ExpenseDto;
import com.example.photobook.entity.ExpenseCategory;
import com.example.photobook.entity.Expense;
import com.example.photobook.entity.Material;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ExpenseMapper extends EntityMapper<ExpenseDto, Expense>{

    @Override
    @Mapping(target = "category", source = "categoryId")
    @Mapping(target = "material", source = "materialId")
    Expense toEntity(ExpenseDto dto);

    @Override
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "materialId", source = "material.id")
    ExpenseDto toDto(Expense entity);

    default ExpenseCategory mapExpenseCategory(UUID id) {
        if (id == null) {
            return null;
        }
        ExpenseCategory category = new ExpenseCategory();
        category.setId(id);
        return category;
    }

    default UUID mapExpenseCategory(ExpenseCategory category) {
        return category == null ? null : category.getId();
    }

    default Material mapMaterial(UUID id) {
        if (id == null) {
            return null;
        }
        Material material = new Material();
        material.setId(id);
        return material;
    }

    default UUID mapMaterial(Material material) {
        return material == null ? null : material.getId();
    }
}
