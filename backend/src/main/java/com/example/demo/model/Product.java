package com.example.demo.model;

import jakarta.persistence.*; // 引入所有 JPA 註解
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PRODUCTS")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "ID", columnDefinition = "INTEGER", nullable = false, unique = true)
    private Long id;
    @Column(name= "NAME", columnDefinition = "TEXT", nullable = false)
    private String name;
    @Column(name= "DESCRIPTION", columnDefinition = "TEXT")
    private String description;
    @Column(name= "PRICE", columnDefinition = "FLOAT", nullable = false)
    private double price;
    @Column(name= "STOCK", columnDefinition = "INTEGER", nullable = false)
    private int stock;
    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private Category category;
    @ManyToOne
    @JoinColumn(name = "OWNER_ID", nullable = false)
    private User owner;
//    private
    
    @Override
    public String toString() {
        return "Product_ID:" + id + '\n'
                + "Product_Name:" + name + '\n'
                + "Product_Description:" + description + '\n'
                + "Product_Price:" + price + '\n'
                + "Product_Category_id:" + (category != null ? category.getName() : "null");
    }
}
