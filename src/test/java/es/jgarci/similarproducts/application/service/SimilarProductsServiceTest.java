package es.jgarci.similarproducts.application.service;

import es.jgarci.similarproducts.domain.model.Product;
import es.jgarci.similarproducts.domain.port.ExistingApisPort;
import es.jgarci.similarproducts.infrastructure.config.SimilarProductsProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimilarProductsServiceTest {

    private ExistingApisPort existingApis;
    private SimilarProductsProps props;
    private SimilarProductsService service;

    @BeforeEach
    void setUp() {
        existingApis = mock(ExistingApisPort.class);
        props = new SimilarProductsProps(4, Duration.ofMillis(200));
        service = new SimilarProductsService(existingApis, props);
    }

    @Test
    void happyPath_returnsProductsInOrder() {
        // given
        when(existingApis.findSimilarIds("1")).thenReturn(Mono.just(List.of("2", "3", "4")));
        when(existingApis.findProductById("2")).thenReturn(Mono.just(new Product("2", "Camisa", new BigDecimal("9.99"), true)));
        when(existingApis.findProductById("3")).thenReturn(Mono.just(new Product("3", "Pantalón", new BigDecimal("19.99"), false)));
        when(existingApis.findProductById("4")).thenReturn(Mono.just(new Product("4", "Gorro", new BigDecimal("29.99"), true)));

        // when
        Mono<List<Product>> result = service.handle("1");

        // then
        StepVerifier.create(result)
                .expectNextMatches(list ->
                        list.size() == 3 &&
                                list.get(0).id().equals("2") &&
                                list.get(1).id().equals("3") &&
                                list.get(2).id().equals("4"))
                .verifyComplete();

        verify(existingApis).findSimilarIds("1");
        verify(existingApis, times(1)).findProductById("2");
        verify(existingApis, times(1)).findProductById("3");
        verify(existingApis, times(1)).findProductById("4");
    }

    @Test
    void timeoutOrError_skipsThatProduct_butContinues() {
        // given
        when(existingApis.findSimilarIds("1")).thenReturn(Mono.just(List.of("2", "3", "4")));
        when(existingApis.findProductById("2")).thenReturn(Mono.just(new Product("2", "Camisa", new BigDecimal("9.99"), true)));
        // Simula timeout por .timeout(props.getTimeout())
        when(existingApis.findProductById("3")).thenReturn(Mono.never());
        // Simula error 404 o 5xx -> onErrorResume vacía en el servicio
        when(existingApis.findProductById("4")).thenReturn(Mono.error(new RuntimeException("Exception")));

        // when
        Mono<List<Product>> result = service.handle("1");

        // then
        StepVerifier.withVirtualTime(() -> result)
                .thenAwait(props.timeout().plusMillis(50))
                .expectNextMatches(list ->
                        list.size() == 1 && list.get(0).id().equals("2"))
                .verifyComplete();
    }
}
