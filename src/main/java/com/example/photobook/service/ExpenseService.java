package com.example.photobook.service;

import com.example.photobook.dto.ExpenseDto;
import com.example.photobook.entity.Expense;
import com.example.photobook.mapper.ExpenseMapper;
import com.example.photobook.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository repository;
    private final ExpenseMapper mapper;
    private final ExpenseCategoryService categoryService;
    private final MaterialService materialService;

    public ExpenseDto create(ExpenseDto dto) {
        Expense expense = mapper.toEntity(dto);
        return mapper.toDto(repository.save(expense));
    }

    public ExpenseDto update(UUID id, ExpenseDto dto) {
        Expense expense = findByUserId(id);
        expense.setName(dto.getName());
        expense.setCategory(categoryService.findByExpenseCategoryId(dto.getCategoryId()));
        expense.setMaterial(materialService.findByMaterialId(dto.getMaterialId()));
        expense.setName(dto.getName());
        expense.setPrice(dto.getPrice());
        expense.setDescription(dto.getDescription());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setReceiptImageUrl(dto.getReceiptImageUrl());
        expense.setExpenseDate(dto.getExpenseDate());

        return mapper.toDto(repository.save(expense));
    }

    public ExpenseDto findById(UUID id) {
        Expense expense = findByUserId(id);
        return mapper.toDto(expense);
    }

    public List<ExpenseDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public void delete(UUID id) {
        Expense expense = findByUserId(id);
        repository.delete(expense);
    }

    private Expense findByUserId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("expense not found"));
    }
}
