package com.example.demo.repository;

import com.example.demo.model.Product;
import com.example.demo.model.Category;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Integer>{
    Optional<Product> findByName(String name);
    List<Product> findByOwner(User owner);
    Optional<Product> findById(Long id);
    Optional<Product> findByNameAndOwner(String name, User owner);
    boolean existsByCategory(Category category);
}
