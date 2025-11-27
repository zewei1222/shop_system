package com.example.demo.dto;

import com.example.demo.model.Product;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProductResponse {
    //這裡決定要從後端回傳甚麼到前端
    //Q. 為什麼要這樣，不直接回傳?
    //A. 因為直接回傳會把owner的密碼也回傳
    //要過濾回傳的內容
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private Long categoryId;
    private String categoryName;
    private Long ownerId;
    private String ownerName;

    // 建構子：負責把 Entity 轉成 DTO
    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();

        if (product.getCategory() != null) {
            this.categoryId = product.getCategory().getId();
            this.categoryName = product.getCategory().getName();
        }
        if (product.getOwner() != null) {
            this.ownerId = product.getOwner().getId();
            this.ownerName = product.getOwner().getUsername();
        }
    }
}