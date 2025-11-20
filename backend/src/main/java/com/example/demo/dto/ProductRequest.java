package com.example.demo.dto;

public class ProductRequest {
    // 前端只需要傳這些，不需要傳 ID、Owner、也不需要傳完整的 Category 物件
    private String name;
    private String description;
    private double price;
    private int stock;
    private String categoryName; // 前端只傳分類名稱字串

    // --- Getters and Setters (務必生成) ---
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
}