package com.example.photobook.service;

import com.example.photobook.dto.ExpenseCategoryDto;
import com.example.photobook.dto.request.ExpenseCategoryPagingRequest;
import com.example.photobook.entity.ExpenseCategory;
import com.example.photobook.mapper.ExpenseCategoryMapper;
import com.example.photobook.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {
    private final ExpenseCategoryRepository repository;
    private final ExpenseCategoryMapper mapper;

    public ExpenseCategoryDto create(ExpenseCategoryDto dto) {
        validateExpenseCategory(dto);
        ExpenseCategory expenseCategory = mapper.toEntity(dto);

        return mapper.toDto(repository.save(expenseCategory));
    }

    public ExpenseCategoryDto update(UUID id, ExpenseCategoryDto dto) {
        validateExpenseCategory(dto);
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

    public Page<ExpenseCategoryDto> findPage(ExpenseCategoryPagingRequest request, Pageable pageable) {
        return repository.findPage(request.getSearch(), pageable).map(mapper::toDto);
    }

    public void delete(UUID id) {
        ExpenseCategory expenseCategory = findByExpenseCategoryId(id);
        repository.delete(expenseCategory);
    }

    public ExpenseCategory findByExpenseCategoryId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("expenseCategory not found"));
    }

    private void validateExpenseCategory(ExpenseCategoryDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
    }
}
