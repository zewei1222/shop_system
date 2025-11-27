package com.example.demo.repository;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Integer>{

    Optional<Product> findById(Long id);
    boolean existsByCategory(Category category);
    // 1. [Admin] 搜尋全部 (模糊搜尋)
    Page<Product> findByNameContaining(String keyword, Pageable pageable);
    // 2. [User] 查詢自己的全部 (無搜尋)
    Page<Product> findByOwner(User owner, Pageable pageable);
    // 3. [User] 搜尋自己的 (模糊搜尋)
    Page<Product> findByOwnerAndNameContaining(User owner, String keyword, Pageable pageable);
}
