package com.microservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class
Product {

    @Id
    @GeneratedValue
    private Long id;
    private String productName;
    private Float price;
    private String sku;
    private String category;

}
