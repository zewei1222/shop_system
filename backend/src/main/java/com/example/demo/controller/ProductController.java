package com.example.demo.controller;


import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    //[C]
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request,
                                                         @AuthenticationPrincipal User currentUser) {
        Product createdProduct = productService.createProduct(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProductResponse(createdProduct));
    }

    // [R]
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            // 新增這兩個參數，預設依 ID 倒序
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // 傳入 Service
        Page<Product> productPage = productService.getProducts(currentUser, keyword, page, size, sortBy, sortDir);

        Page<ProductResponse> responsePage = productPage.map(ProductResponse::new);

        return ResponseEntity.ok(responsePage);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @RequestBody ProductRequest request) {

        Product updatedProduct = productService.updateProduct(request, currentUser, id);
        return ResponseEntity.ok(new ProductResponse(updatedProduct));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {

        productService.deleteProduct(id, currentUser);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<Long> ids, @AuthenticationPrincipal User currentUser) {

        productService.deleteProductsBatch(ids, currentUser);

        return ResponseEntity.noContent().build();
    }


}