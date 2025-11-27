package com.example.demo.service;

import com.example.demo.model.Category;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // 1. 改用建構子注入，程式碼更乾淨
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // 給 ProductService 用的內部方法
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("找不到分類: " + name));
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // [C] 新增分類
    @Transactional
    public Category createCategory(Category category) {
        // 注意：Controller 層已經透過 @AuthenticationPrincipal 拿到 User
        // 這裡可以再次檢查權限，或者相信 Controller 的判斷
        // 為了簡化，這裡專注於業務邏輯：檢查名稱重複
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new RuntimeException("分類名稱 '" + category.getName() + "' 已存在");
        }
        return categoryRepository.save(category);
    }

    // [D] 刪除分類
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("找不到分類 ID: " + categoryId));

        // 防呆：如果分類下還有商品，禁止刪除
        if (productRepository.existsByCategory(category)) {
            throw new RuntimeException("無法刪除！分類 '" + category.getName() + "' 下尚有商品。");
        }

        categoryRepository.delete(category);
    }

    // [U] 更新分類 (Optional, 如果你有做編輯功能的話)
    @Transactional
    public Category updateCategory(Long id, Category newInfo) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到分類"));

        // 檢查撞名 (排除自己)
        categoryRepository.findByName(newInfo.getName()).ifPresent(c -> {
            if (!c.getId().equals(id)) {
                throw new RuntimeException("分類名稱已存在");
            }
        });

        category.setName(newInfo.getName());
        return categoryRepository.save(category);
    }
}