package es.jgarci.similarproducts.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "external.existing-apis")
@Validated
public record ExistingApisProps(
        @NotBlank String baseUrl,
        @DurationMin(millis = 800) Duration httpTimeout
) {
}
