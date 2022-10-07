package com.microservice.productclient;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@EnableCirciutBreaker
@EnableBinding(Source.class)
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ProductClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductClientApplication.class, args);
	}

}

@RestController
@RequestMapping("/products")
class ProductApiGatewayRestController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Bean
	private RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Autowired
	private RestTemplate restTemplate;

	public Collection<Product> getProductsFallback() {
		return new ArrayList<>();
	}

	@Autowired
	private Source source;

	@RequestMapping(method = RequestMethod.POST)
	public void writeProduct(@RequestBody Product r) {
		Message<String> msg = MessageBuilder.withPayload(r).build();
		this.source.output().send(msg);
	}

	@HystrixCommand(fallbackMethod = "getProductsFallback")
	@RequestMapping(method = RequestMethod.GET, value = "/products")
	public Collection<Product> getProducts() {

		ParameterizedTypeReference<Resources<Product>> ptr = new ParameterizedTypeReference<Resources<Product>>() {
		};

		ResponseEntity<Resources<Product>> entity = this.restTemplate
				.exchange("http://localhost:8000/products", HttpMethod.GET, null, ptr);

		return entity
				.getBody()
				.getContent()
				.stream()
				.map(Product)
				.collect(Collectors.toList());
	}
}

class Product {

	private Long id;
	private String productName;
	private Float price;
	private String sku;
	private String category;

}