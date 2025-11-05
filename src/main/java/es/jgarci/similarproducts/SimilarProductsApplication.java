package es.jgarci.similarproducts;

import es.jgarci.similarproducts.infrastructure.config.ExistingApisProps;
import es.jgarci.similarproducts.infrastructure.config.SimilarProductsProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = {
        SimilarProductsProps.class,
        ExistingApisProps.class
})
public class SimilarProductsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimilarProductsApplication.class, args);
    }
}
