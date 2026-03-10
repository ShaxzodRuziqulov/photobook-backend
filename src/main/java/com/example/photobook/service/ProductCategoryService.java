package com.example.photobook.service;

import com.example.photobook.dto.ProductCategoryDto;
import com.example.photobook.entity.ProductCategory;
import com.example.photobook.mapper.ProductCategoryMapper;
import com.example.photobook.repository.ProductCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository repository;
    private final ProductCategoryMapper mapper;

    public ProductCategoryDto create(ProductCategoryDto dto) {
        ProductCategory productCategory = mapper.toEntity(dto);

        return mapper.toDto(repository.save(productCategory));
    }

    public ProductCategoryDto update(UUID id, ProductCategoryDto dto) {
        ProductCategory productCategory = findByProductCategoryId(id);
        productCategory.setName(dto.getName());
        productCategory.setKind(dto.getKind());
        productCategory.setDefaultPages(dto.getDefaultPages());

        return mapper.toDto(repository.save(productCategory));
    }

    public ProductCategoryDto findById(UUID id) {
        ProductCategory productCategory = findByProductCategoryId(id);
        return mapper.toDto(productCategory);
    }

    public List<ProductCategoryDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public void delete(UUID id) {
        ProductCategory productCategory = findByProductCategoryId(id);
        repository.delete(productCategory);
    }

    public ProductCategory findByProductCategoryId(UUID productCategoryId) {
        return repository.findById(productCategoryId).orElseThrow(() -> new EntityNotFoundException("Product category not found"));
    }
}
