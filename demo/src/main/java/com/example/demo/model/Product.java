package com.example.demo.model;

import jakarta.persistence.*; // 引入所有 JPA 註解

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    private String description;
    @Column(nullable = false)
    private double price;
    @Column(nullable = false)
    private int stock;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Product() {}

    public Product(String name, String description, double price, int stock, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    public void setId(Long id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public void setPrice(double price){
        this.price = price;
    }
    public void setStock(int stock){
        this.stock = stock;
    }
    public void setCategory(Category category){
        this.category = category;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public double getPrice() {
        return price;
    }
    public int getStock(){
        return stock;
    }
    public Category getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "Product_ID:" + id + '\n'
                + "Product_Name:" + name + '\n'
                + "Product_Description:" + description + '\n'
                + "Product_Price:" + price + '\n'
                + "Product_Category_id:" + (category != null ? category.getName() : "null");
    }
}
