package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "id", nullable = false, unique = true)
    private Long id;

    @Column(name= "name", nullable = false)
    private String name;

    @Column(name= "description")
    private String description;

    @Column(name= "price", nullable = false)
    private double price;

    @Column(name= "stock", nullable = false)
    private int stock;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Override
    public String toString() {
        return "Product_ID:" + id + '\n'
                + "Product_Name:" + name + '\n'
                + "Product_Description:" + description + '\n'
                + "Product_Price:" + price + '\n'
                + "Product_Category_id:" + (category != null ? category.getName() : "null");
    }
}
