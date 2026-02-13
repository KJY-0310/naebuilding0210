package com.example.naebuilding.repository;

import com.example.naebuilding.domain.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByName(String name);

    List<CategoryEntity> findAllByOrderByCategoryIdDesc();
    List<CategoryEntity> findAllByActiveTrueOrderByNameAsc();
}
