package com.example.demo.service;

import com.example.demo.model.Category;
import com.example.demo.model.User;
import com.example.demo.model.Role;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository; // 1. 引入 ProductRepository

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository; // 2. 注入 ProductRepository

    // --- [新增] 提供給 ProductService 呼叫的方法 ---
    // 讓 ProductService 不需要直接存取 CategoryRepository
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("[Service Error]: Category '" + name + "' not found"));
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category addCategory(Category category, User currentUser) {
        if (!currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new RuntimeException("[Service Error]: 權限不足");
        }
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new RuntimeException("[Service Error]: Category " + category.getName() + " already exists");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId, User currentUser) {
        if (!currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new RuntimeException("[Service Error]: 權限不足");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("[Service Error]: Category " + categoryId + " not found"));

        // 3. [實作 TODO] 檢查是否有商品正在使用該分類
        // 防止刪除分類後，導致商品變成孤兒或資料庫報錯
        if (productRepository.existsByCategory(category)) {
            throw new RuntimeException("[Service Error]: 無法刪除！分類 '" + category.getName() + "' 下尚有商品，請先移除相關商品。");
        }

        categoryRepository.delete(category);
        System.out.println("[Service]: Category Deleted Successfully");
    }

    @Transactional
    public Category updateCategory(Long categoryId, String newName, User currentUser) {
        if (!currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new RuntimeException("[Service Error]: 權限不足");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("[Service Error]: Category " + categoryId + " not found"));

        // 檢查新名稱是否跟別的分類衝突 (排除自己)
        categoryRepository.findByName(newName).ifPresent(c -> {
            if (!c.getId().equals(categoryId)) {
                throw new RuntimeException("[Service Error]: Category name '" + newName + "' has been used");
            }
        });

        category.setName(newName);
        return categoryRepository.save(category);
    }
}