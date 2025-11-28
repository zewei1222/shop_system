package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "商品名稱不能為空")
    @Size(max = 30, message = "商品名稱過長，請控制在 30 字以內")
    private String name;

    @Size(max = 100, message = "商品敘述過長，請控制在 100 字以內")
    private String description;

    @Positive(message = "價格必須大於 0")
    @Max(value = 10000000, message = "價格過高，系統上限為一千萬")
    private double price;

    @PositiveOrZero(message = "庫存不能為負數")
    @Max(value = 10000, message = "庫存數量過大")
    private int stock;

    @NotNull(message = "必須選擇分類")
    private Long categoryId;
}