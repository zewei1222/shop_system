package com.example.demo.controller;

import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.model.Product;
import com.example.demo.model.Category;
import com.example.demo.model.User;
import com.example.demo.service.ProductService;
import com.example.demo.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService  productService;

    @Autowired
    private UserRepository userRepository;

    private User getMockCurrentUser() {
        // 假設固定用 admin 登入 (你需要確保資料庫有這個人)
        return userRepository.findByName("wen") // 或你的測試帳號
                .orElseThrow(() -> new RuntimeException("Mock user not found"));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        User currentUser = getMockCurrentUser();

        // 1. 從 Service 拿到 Entity 列表
        List<Product> products = productService.getAllProducts(currentUser);

        // 2. 轉換成 DTO 列表 (使用 Stream API 比較簡潔)
        List<ProductResponse> responseList = products.stream()
                .map(ProductResponse::new) // 呼叫剛剛寫的建構子
                .toList(); // 或者 .collect(Collectors.toList()) 如果你是 Java 16 以下

        return ResponseEntity.ok(responseList);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        User currentUser = getMockCurrentUser();

        // A. 將 DTO 轉成 Entity (手動轉換)
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        // 處理分類：前端傳字串 -> 我們包成物件給 Service 處理
        product.setCategory(new Category(null, request.getCategoryName()));

        // B. 呼叫 Service
        Product createdProduct = productService.createProduct(product, currentUser);

        // C. 回傳 201 Created
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProductResponse(createdProduct));
    }

    @PutMapping("/{id}") // 網址: /api/products/10
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                 @RequestBody ProductRequest request) {
        User currentUser = getMockCurrentUser();

        // 將 DTO 轉成 Entity
        Product newInfo = new Product();
        newInfo.setName(request.getName());
        newInfo.setDescription(request.getDescription());
        newInfo.setPrice(request.getPrice());
        newInfo.setStock(request.getStock());

        if (request.getCategoryName() != null) {
            newInfo.setCategory(new Category(null, request.getCategoryName()));
        }

        Product updatedProduct = productService.updateProduct(id, newInfo, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProductResponse(updatedProduct));
    }

    // --- 4. 刪除商品 (DELETE) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        User currentUser = getMockCurrentUser();

        productService.deleteProduct(id, currentUser);

        return ResponseEntity.noContent().build(); // 回傳 204 No Content (刪除成功的標準回應)
    }

    // --- 5. 批次刪除 (DELETE) ---
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<Long> ids) {
        User currentUser = getMockCurrentUser();

        // 現在 Service 接收的就是 ids，直接傳進去就好
        productService.deleteProductsBatch(ids, currentUser);

        return ResponseEntity.noContent().build();
    }
}
