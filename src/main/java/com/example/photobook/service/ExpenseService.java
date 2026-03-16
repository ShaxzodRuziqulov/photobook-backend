package com.example.photobook.service;

import com.example.photobook.dto.ExpenseDto;
import com.example.photobook.dto.request.ExpensePagingRequest;
import com.example.photobook.entity.Expense;
import com.example.photobook.entity.Upload;
import com.example.photobook.entity.enumirated.OwnerType;
import com.example.photobook.mapper.ExpenseMapper;
import com.example.photobook.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final UploadService uploadService;

    public ExpenseDto create(ExpenseDto dto) {
        validateExpense(dto);
        Expense expense = mapper.toEntity(dto);
        if (dto.getCategoryId() != null) {
            expense.setCategory(categoryService.findByExpenseCategoryId(dto.getCategoryId()));
        }
        if (dto.getMaterialId() != null) {
            expense.setMaterial(materialService.findByMaterialId(dto.getMaterialId()));
        }
        Expense savedExpense = repository.save(expense);
        attachUploadIfPresent(savedExpense, dto.getUploadId());
        return mapper.toDto(savedExpense);
    }

    public ExpenseDto update(UUID id, ExpenseDto dto) {
        validateExpense(dto);
        Expense expense = findEntityById(id);
        expense.setName(dto.getName());
        if (dto.getCategoryId() != null) {
            expense.setCategory(categoryService.findByExpenseCategoryId(dto.getCategoryId()));
        }
        if (dto.getMaterialId() != null) {
            expense.setMaterial(materialService.findByMaterialId(dto.getMaterialId()));
        }
        expense.setPrice(dto.getPrice());
        expense.setDescription(dto.getDescription());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setReceiptImageUrl(dto.getReceiptImageUrl());
        expense.setExpenseDate(dto.getExpenseDate());
        Expense savedExpense = repository.save(expense);
        attachUploadIfPresent(savedExpense, dto.getUploadId());
        return mapper.toDto(savedExpense);
    }

    public ExpenseDto findById(UUID id) {
        Expense expense = findEntityById(id);
        return mapper.toDto(expense);
    }

    public List<ExpenseDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public Page<ExpenseDto> findPage(ExpensePagingRequest request, Pageable pageable) {
        return repository.findPage(
                request.getSearch(),
                request.getCategoryId(),
                request.getMaterialId(),
                request.getPaymentMethod(),
                pageable).map(mapper::toDto);
    }

    public void delete(UUID id) {
        Expense expense = findEntityById(id);
        uploadService.deleteOwnedUpload(OwnerType.EXPENSE, expense.getId(), null);
        repository.delete(expense);
    }

    private Expense findEntityById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("expense not found"));
    }

    private void validateExpense(ExpenseDto dto) {
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("category_id is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (dto.getPrice() == null || dto.getPrice().signum() < 0) {
            throw new IllegalArgumentException("price must be greater than or equal to 0");
        }
        if (dto.getExpenseDate() == null) {
            throw new IllegalArgumentException("expense_date is required");
        }
    }

    private void attachUploadIfPresent(Expense expense, UUID uploadId) {
        if (uploadId == null) {
            return;
        }
        Upload upload = uploadService.attachToOwner(uploadId, OwnerType.EXPENSE, expense.getId());
        expense.setUpload(upload);
        expense.setReceiptImageUrl(uploadService.buildFileUrl(upload));
        repository.save(expense);
    }
}
