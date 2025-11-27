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

    //[R]
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,     // 預設第 0 頁
            @RequestParam(defaultValue = "10") int size,    // 預設一頁 10 筆
            @RequestParam(defaultValue = "") String keyword // 預設無關鍵字
    ) {

        Page<Product> productPage = productService.getProducts(currentUser, keyword, page, size);

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