package com.example.demo.service;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    // 驗證並獲取資料庫中的分類
    private void validateCategory(Product product) {
        if (product.getCategory() == null) {
            throw new RuntimeException("[Service Error]: Category cannot be null");
        }
        // 透過 CategoryService 確保分類存在並取得完整實體
        Category dbCategory = categoryService.getCategoryByName(product.getCategory().getName());
        product.setCategory(dbCategory);
    }

    // [C] Create: 新增商品 (允許同名)
    @Transactional
    public Product createProduct(Product product, User currentUser) {
        // 1. 設定 Owner
        product.setOwner(currentUser);

        // 2. 驗證分類
        validateCategory(product);

        // 3. [移除] 檢查重名的邏輯
        // if (productRepository.findByNameAndOwner(...).isPresent()) { ... } <---這段刪掉

        // 4. 直接儲存
        // 即使資料庫裡已經有 "iPhone 15"，這裡會存入一筆新的 "iPhone 15"，但 ID 不同
        Product savedProduct = productRepository.save(product);
        System.out.println("[Service]: New Product '" + savedProduct.getName() + "' Created (ID: " + savedProduct.getId() + ")");
        return savedProduct;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts(User currentUser) {

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            System.out.println("[Service]: Admin: " + currentUser.getName() + " Searching for all products");
            return productRepository.findAll();
        }

        List<Product> productList = productRepository.findByOwner(currentUser);
        System.out.println("[Service]: User: " + currentUser.getName() + " Searching for owned products");
        return productList;
    }

    @Transactional
    public void deleteProduct(Long productId, User currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("[Service Error]: Product ID " + productId + " not found"));

        if (currentUser.getRole() != Role.ROLE_ADMIN && !product.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("[Service Error]: 權限不足");
        }

        productRepository.delete(product);
        System.out.println("[Service]: Product '" + product.getName() + "' Delete Successfully");
    }

    @Transactional
    public void deleteProductsBatch(List<Long> ids, User currentUser) {
        for (Long id : ids) {
            deleteProduct(id, currentUser);
        }
        System.out.println("[Service]: Products Delete Successfully");
    }

    // [U] Update: 修改商品 (依據 ID)
    @Transactional
    public Product updateProduct(Long productId, Product newInfo, User currentUser) {
        // 1. 先用 ID 找出舊資料 (這步最關鍵)
        Product productDb = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("[Service Error]: 找不到商品 ID: " + productId));

        // 2. 權限檢查 (Admin 或 擁有者)
        // 注意：這裡的邏輯跟 deleteProduct 一樣
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        boolean isOwner = productDb.getOwner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("[Service Error]: 權限不足，無法修改他人商品");
        }

        // 3. 更新欄位 (這裡就可以自由改名了！)
        productDb.setName(newInfo.getName());
        productDb.setPrice(newInfo.getPrice());
        productDb.setDescription(newInfo.getDescription());
        productDb.setStock(newInfo.getStock()); // 直接覆蓋庫存 (例如從 10 改成 20)

        // 4. 如果有換分類，要重新驗證並更新
        if (newInfo.getCategory() != null) {
            validateCategory(newInfo); // 確保新分類存在
            productDb.setCategory(newInfo.getCategory());
        }

        // 5. 儲存
        Product updatedProduct = productRepository.save(productDb);
        System.out.println("[Service]: Product '" + updatedProduct.getName() + "' Updated Successfully");
        return updatedProduct;
    }
}