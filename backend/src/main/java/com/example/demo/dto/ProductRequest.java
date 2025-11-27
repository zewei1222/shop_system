package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "商品名稱不能為空")
    private String name;

    private String description;

    @Positive(message = "價格必須大於 0")
    private double price;

    @Positive(message = "庫存必須大於 0")
    private int stock;

    @NotNull(message = "必須選擇分類")
    private Long categoryId;
}