package com.example.photobook.service;

import com.example.photobook.entity.ProductCategory;
import com.example.photobook.repository.ProductCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository repository;

    public ProductCategory findByProductCategoryId(UUID productCategoryId) {
        return repository.findById(productCategoryId).orElseThrow(() -> new EntityNotFoundException("Product category not found"));
    }
}
