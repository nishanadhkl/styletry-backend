package com.styletry.backend.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProductRequest {
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
    private Integer stock;
    private String size;
    private String color;
}