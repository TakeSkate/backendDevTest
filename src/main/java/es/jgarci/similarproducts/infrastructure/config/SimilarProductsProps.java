package es.jgarci.similarproducts.infrastructure.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "similar-products")
@Validated
public record SimilarProductsProps(
        @Min(1) @Max(64) int concurrency,
        @NotNull Duration timeout
) {
}
