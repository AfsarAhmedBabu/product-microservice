package com.microservice.productservice;

import com.microservice.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Stream;

@EnableDiscoveryClient
@IntegrationComponentScan
@EnableBinding(Sink.class)
@SpringBootApplication
public class ProductServiceApplication {

	@RepositoryRestResource
	interface ProductRepository extends JpaRepository<Product, Long> {

		@RestResource(path = "by-name")
		Collection<Product> findByProductName(@Param("productName") String productName);

	}

	@Component
	class DummyDataCLR implements CommandLineRunner {

		@Autowired
		private ProductRepository productRepository;

		@Override
		public void run(String... args) throws Exception {

			Product product1 = new Product(1L, "Lenovo Ideapad", 70000F, "98e342", "laptop");
			Product product2 = new Product(2L, "Dell Inspiron", 90000F, "76e354", "laptop");

			Stream.of(product1, product2)
					.forEach(product -> {
						productRepository.save(product);
					});

			productRepository.findAll().forEach(product -> {
				System.out.println(product.getProductName());
			});

		}
	}

	@RefreshScope
	@RestController
	class MessageRestController {

		private final String value;

		@Autowired
		public MessageRestController(@Value("${message}") String value) {
			this.value = value;
		}

		@RequestMapping("/message")
		String read() {
			return this.value;
		}

	}

	class ProductProcessor {

		private ProductRepository productRepository;

		public ProductProcessor(ProductRepository productRepository) {
			this.productRepository = productRepository;
		}

		@ServiceActivator(inputChannel = Sink.INPUT)
		public void acceptNewProduct(Product product) {
			this.productRepository.save(product);
		}

	}

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

}
