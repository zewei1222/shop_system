package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.model.User;
import com.example.demo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category") // 前端呼叫路徑
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // [R] 取得所有分類 (公開或需登入皆可，這裡設為需登入)
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // [C] 新增分類
    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestBody Category category,
            @AuthenticationPrincipal User currentUser) {
        // 這裡可以加權限判斷，例如只有 ADMIN 能建分類
        // if (currentUser.getRole() != Role.ADMIN) throw ...
        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    // [D] 刪除分類
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}