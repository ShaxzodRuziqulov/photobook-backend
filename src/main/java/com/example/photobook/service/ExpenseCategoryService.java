package com.example.photobook.service;

import com.example.photobook.dto.ExpenseCategoryDto;
import com.example.photobook.entity.ExpenseCategory;
import com.example.photobook.mapper.ExpenseCategoryMapper;
import com.example.photobook.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {
    private final ExpenseCategoryRepository repository;
    private final ExpenseCategoryMapper mapper;

    public ExpenseCategoryDto create(ExpenseCategoryDto dto) {
        ExpenseCategory expenseCategory = mapper.toEntity(dto);

        return mapper.toDto(repository.save(expenseCategory));
    }

    public ExpenseCategoryDto update(UUID id, ExpenseCategoryDto dto) {
        ExpenseCategory expenseCategory = findByExpenseCategoryId(id);
        expenseCategory.setName(dto.getName());
        return mapper.toDto(repository.save(expenseCategory));
    }

    public ExpenseCategoryDto findById(UUID id) {
        ExpenseCategory expenseCategory = findByExpenseCategoryId(id);
        return mapper.toDto(expenseCategory);
    }

    public List<ExpenseCategoryDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public void delete(UUID id) {
        ExpenseCategory expenseCategory = findByExpenseCategoryId(id);
        repository.delete(expenseCategory);
    }

    public ExpenseCategory findByExpenseCategoryId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("expenseCategory not found"));
    }
}
