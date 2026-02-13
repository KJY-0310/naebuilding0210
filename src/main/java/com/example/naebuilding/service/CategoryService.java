package com.example.naebuilding.service;

import com.example.naebuilding.domain.CategoryEntity;
import com.example.naebuilding.dto.admin.CategoryCreateRequest;
import com.example.naebuilding.dto.admin.CategoryResponse;
import com.example.naebuilding.exception.NotFoundException;
import com.example.naebuilding.repository.CategoryRepository;
import com.example.naebuilding.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listForAdmin() {
        return categoryRepository.findAllByOrderByCategoryIdDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> listActiveNamesForPublic() {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(CategoryEntity::getName)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryCreateRequest req) {
        String name = req.name().trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("카테고리 이름은 비어있을 수 없습니다.");
        }
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다.");
        }

        CategoryEntity saved = categoryRepository.save(new CategoryEntity(name));
        return toDto(saved);
    }

    // ✅ 비활성(소프트 삭제)
    @Transactional
    public void deactivate(Long categoryId) {
        CategoryEntity c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND"));
        c.deactivate();
    }

    // ✅ 활성(복구)
    @Transactional
    public void activate(Long categoryId) {
        CategoryEntity c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND"));
        c.activate();
    }

    // ✅ 실제 삭제 (하드삭제) + 사용중이면 삭제 금지
    @Transactional
    public void deleteHard(Long categoryId) {
        CategoryEntity c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND"));

        long used = requestRepository.countByCategory(c.getName());
        if (used > 0) {
            throw new IllegalStateException("사용 중인 카테고리는 삭제할 수 없습니다. (사용 민원: " + used + "건)");
        }

        categoryRepository.delete(c);
    }

    private CategoryResponse toDto(CategoryEntity e) {
        return new CategoryResponse(
                e.getCategoryId(),
                e.getName(),
                e.isActive(),
                e.getCreatedAt()
        );
    }
}
