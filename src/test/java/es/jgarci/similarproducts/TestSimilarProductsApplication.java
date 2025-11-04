package es.jgarci.similarproducts;

import org.springframework.boot.SpringApplication;

public class TestSimilarProductsApplication {

	public static void main(String[] args) {
		SpringApplication.from(SimilarProductsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
