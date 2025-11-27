package com.example.demo.service;

import com.example.demo.dto.ProductRequest;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ProductService {
    //資料庫關聯
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    //[C]
    @Transactional
    public Product createProduct(ProductRequest request, User owner) {
        //2. 商品是甚麼分類
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category ID " + request.getCategoryId() + " not found"));
        //3. DTO to Entity
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setStock(request.getStock());
        product.setCategory(category);
        product.setOwner(owner);

        return productRepository.save(product);
    }
    //[R]查詢商品
    @Transactional(readOnly = true)
    public Page<Product> getProducts(User user, String keyword, int page, int size){
        // 1. 準備分頁設定 (Pageable)
        // 參數：(第幾頁, 一頁幾筆, 排序方式)
        // 這裡設定：依照 ID 倒序排列 (最新的商品排在最前面)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        // 2. 判斷是否有關鍵字 (防止 null 或 空白字串)
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        // 3. 依照身分決定搜尋範圍
        // 管理員
        if (user.getRole() == Role.ROLE_ADMIN) {
            if (hasKeyword){
                //可以查到所有該關鍵字的商品
                return productRepository.findByNameContaining(keyword, pageable);
            }else{
                //查詢所有商品
                return productRepository.findAll(pageable);
            }
        // 一般用戶
        }else if(user.getRole() == Role.ROLE_USER){
            if (hasKeyword){
                // 只能查到自己的有關鍵字的商品
                return productRepository.findByOwnerAndNameContaining(user, keyword, pageable);
            }else{
                //查詢自己有的所有商品
                return productRepository.findByOwner(user, pageable);
            }
        }else{
            throw new RuntimeException("查詢錯誤");
        }
    }

    //[U]更新商品
    @Transactional
    public Product updateProduct(ProductRequest request, User user, Long productId) {
        //1. 誰更新此商品

        //2. 更新什麼商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product ID " + productId + " not found"));
        //3. 判斷有無權限進行此操作
        if(!(user.getRole() == Role.ROLE_ADMIN) && !user.getId().equals(product.getOwner().getId())){
            throw new RuntimeException("你沒有權限執行此操作");
        }
        //4. 商品是甚麼分類
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category ID " + request.getCategoryId() + " not found"));
        //5. DTO to Entity
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);
        //6. 執行儲存商品
        return productRepository.save(product);
    }

    //[D]刪除單一商品
    @Transactional
    public void deleteProduct(Long productId, User user){
        //1. 誰刪除此商品
        //2. 刪除甚麼商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product ID " + productId + " not found"));
        //3. 判斷有無權限進行此操作
        if(!(user.getRole() == Role.ROLE_ADMIN) &&  !user.getId().equals(product.getOwner().getId())){
            throw new RuntimeException("你沒有權限執行此操作");
        }
        //4. 執行刪除商品
        productRepository.delete(product);
    }

    //[D]批次刪除商品
    @Transactional
    public void deleteProductsBatch(List<Long> productIds, User user){
        for (Long id: productIds){
            deleteProduct(id, user);
        }
    }
}