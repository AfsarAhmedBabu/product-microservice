package com.microservice.productclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ProductClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductClientApplication.class, args);
	}

}
