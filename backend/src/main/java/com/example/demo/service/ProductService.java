package com.example.demo.service;

import com.example.demo.dto.ProductRequest;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

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
    // [R] 查詢商品 (支援動態排序)
    @Transactional(readOnly = true)
    public Page<Product> getProducts(User user, String keyword, int page, int size, String sortBy, String sortDir) {

        // 1. 欄位對應 (Mapping)
        // 防止前端傳來 "categoryName" 但資料庫不認識，要轉成 "category.name"
        String sortField = switch (sortBy) {
            case "ownerName" -> "owner.username";   // 對應 User 的 username
            case "categoryName" -> "category.name"; // 對應 Category 的 name
            default -> sortBy; // id, name, price, stock 這些欄位名稱一致，直接用
        };

        // 2. 建立排序物件 (動態決定是升序還是降序)
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        // 3. ★ 關鍵：將動態的 sort 放進 PageRequest
        Pageable pageable = PageRequest.of(page, size, sort);

        // 4. 判斷是否有關鍵字
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        // 5. 執行查詢 (維持原樣)
        if (user.getRole() == Role.ROLE_ADMIN) {
            if (hasKeyword) {
                return productRepository.findByNameContaining(keyword, pageable);
            } else {
                return productRepository.findAll(pageable);
            }
        } else if (user.getRole() == Role.ROLE_USER) {
            if (hasKeyword) {
                return productRepository.findByOwnerAndNameContaining(user, keyword, pageable);
            } else {
                return productRepository.findByOwner(user, pageable);
            }
        } else {
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