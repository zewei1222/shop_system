package com.example.demo.service;
//import database
import com.example.demo.model.Product;
import com.example.demo.model.Category;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.CategoryRepository;
//
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
@Service
public class ProductService {
    private final List<Product> productBuffer =  new ArrayList<>();

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    //Add Product
    public Product addProductToBuffer(Product product){
        product.setId(null);
        productBuffer.add(product);
        System.out.println("[Service]: Add '" +product.getName()+ "' to The Buffer");
        return product;
    }
    //Show ProductList
    public List<Product> getProductsFromBuffer(){
        System.out.println("[Service]: Get Buffer(Total "+ productBuffer.size() + ")");
        return productBuffer;
    }
    //
    public void clearBuffer(){
        System.out.println("[Service]: Clear Buffer");
        productBuffer.clear();
    }

    @Transactional
    public List<Product> saveBufferToDatabase(){
        //
        if (productBuffer.isEmpty()) {
            return new ArrayList<>();
        }
        //
        System.out.println("[Service]: Update " + productBuffer.size() + " Products to Database");

        Map<String, Category> categoryCache = new HashMap<>();
        for (Product p:productBuffer){
            Category category = p.getCategory();
            String categoryName = category.getName();

            if (categoryCache.containsKey(categoryName)) {
                // 是 -> 將這個商品關聯到「快取中」的那個 Category 物件
                p.setCategory(categoryCache.get(categoryName));
            } else {
                Optional<Category> dbCategory = categoryRepository.findByName(categoryName);

                if (dbCategory.isPresent()) {

                    Category existingCategory = dbCategory.get();
                    p.setCategory(existingCategory);
                    categoryCache.put(categoryName, existingCategory);
                } else {
                    Category newCategory = categoryRepository.save(category);
                    p.setCategory(newCategory);
                    categoryCache.put(categoryName, newCategory);
                    System.out.println("[Service]: New Category '" + newCategory.getName() + "' Created");
                }
            }
            //
            p.setId(null);
            //
        }
        List<Product> savedProducts = productRepository.saveAll(productBuffer);
        productBuffer.clear();
        System.out.println("[Service]: Save successful, " + savedProducts.size()
                + " Product Saved in Database. Buffer has been clear");
        return savedProducts;
    }
}
