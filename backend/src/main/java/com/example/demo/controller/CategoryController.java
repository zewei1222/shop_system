    package com.example.demo.controller;

    import com.example.demo.model.Category;
    import com.example.demo.model.User;
    import com.example.demo.service.CategoryService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Map; // 用於接收簡單的 JSON Body

    @RestController
    @RequestMapping("/category")
    @RequiredArgsConstructor
    public class CategoryController {

        private final CategoryService categoryService;

        @GetMapping
        public ResponseEntity<List<Category>> getAllCategories() {
            return ResponseEntity.ok(categoryService.getAllCategories());
        }

        @PostMapping
        public ResponseEntity<Category> createCategory(
                @RequestBody Category category,
                @AuthenticationPrincipal User currentUser) {
            return ResponseEntity.ok(categoryService.createCategory(category, currentUser));
        }

        // ★ 新增：編輯分類 API
        @PutMapping("/{id}")
        public ResponseEntity<Category> updateCategory(
                @PathVariable Long id,
                @RequestBody Map<String, String> payload, // 前端傳來 { "name": "新名稱" }
                @AuthenticationPrincipal User currentUser) {

            String newName = payload.get("name");
            return ResponseEntity.ok(categoryService.updateCategory(id, newName, currentUser));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteCategory(
                @PathVariable Long id,
                @AuthenticationPrincipal User currentUser) {
            categoryService.deleteCategory(id, currentUser);
            return ResponseEntity.noContent().build();
        }
    }