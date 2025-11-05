package es.jgarci.similarproducts.application.service;

import es.jgarci.similarproducts.domain.model.Product;
import es.jgarci.similarproducts.domain.port.ExistingApisPort;
import es.jgarci.similarproducts.infrastructure.config.SimilarProductsProps;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@SpringBootTest
@ActiveProfiles("test")
public class SimilarProductsServiceIT {
    @Autowired
    SimilarProductsService service;

    @MockitoBean
    ExistingApisPort existingApis;
    @MockitoBean
    SimilarProductsProps props;


    @BeforeEach
    void setup() {
        when(props.concurrency()).thenReturn(2);
        when(props.timeout()).thenReturn(Duration.ofMillis(150));
    }

    @Test
    void handle_filtersTimedOutAndErroredDetails() {
        when(existingApis.findSimilarIds("X"))
                .thenReturn(Mono.just(List.of("1", "2", "3")));

        // 1 -> rápido OK
        when(existingApis.findProductById("1"))
                .thenReturn(Mono.just(new Product("1", "P1", new BigDecimal("9.99"), true))
                        .delayElement(Duration.ofMillis(50)));

        // 2 -> lento (supera timeout -> se descarta por onErrorResume)
        when(existingApis.findProductById("2"))
                .thenReturn(Mono.just(new Product("2", "P2", new BigDecimal("19.99"), true))
                        .delayElement(Duration.ofMillis(250)));

        // 3 -> error (se descarta por onErrorResume)
        when(existingApis.findProductById("3"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(service.handle("X"))
                .expectNextMatches(list ->
                        list.size() == 1 &&
                                "1".equals(list.get(0).id()) &&
                                new BigDecimal("9.99").equals(list.get(0).price()))
                .verifyComplete();

        verify(existingApis).findSimilarIds("X");
        verify(existingApis).findProductById("1");
        verify(existingApis).findProductById("2");
        verify(existingApis).findProductById("3");
        verifyNoMoreInteractions(existingApis);
    }
}
