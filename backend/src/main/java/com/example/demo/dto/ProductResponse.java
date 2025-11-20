package com.example.demo.dto;

import com.example.demo.model.Product;

public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String categoryName;
    private String ownerName; // 我們只回傳名字，絕對不回傳密碼！

    // 建構子：負責把 Entity 轉成 DTO
    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();

        // 處理關聯物件，只拿需要的欄位
        if (product.getCategory() != null) {
            this.categoryName = product.getCategory().getName();
        }

        if (product.getOwner() != null) {
            this.ownerName = product.getOwner().getName(); // 只拿名字
        }
    }

    // --- Getters and Setters (務必生成) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}