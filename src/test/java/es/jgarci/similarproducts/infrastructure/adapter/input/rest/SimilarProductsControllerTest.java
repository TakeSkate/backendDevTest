package es.jgarci.similarproducts.infrastructure.adapter.input.rest;

import es.jgarci.similarproducts.api.model.ProductDetail;
import es.jgarci.similarproducts.application.service.SimilarProductsUseCase;
import es.jgarci.similarproducts.domain.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimilarProductsControllerTest {

    @Test
    void returnsFluxBodyOk() {
        // mocks
        SimilarProductsUseCase useCase = mock(SimilarProductsUseCase.class);
        ProductMapper mapper = mock(ProductMapper.class);
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        // controller
        SimilarProductsController controller = new SimilarProductsController(useCase, mapper);

        // given
        List<Product> domain = List.of(
                new Product("2", "A", new BigDecimal("9.99"), true),
                new Product("3", "B", new BigDecimal("19.99"), false)
        );
        when(useCase.handle("1")).thenReturn(Mono.just(domain));

        ProductDetail dto2 = new ProductDetail().id("2").name("A").price(new BigDecimal("9.99")).availability(true);
        ProductDetail dto3 = new ProductDetail().id("3").name("B").price(new BigDecimal("19.99")).availability(false);

        when(mapper.toDto(domain.get(0))).thenReturn(dto2);
        when(mapper.toDto(domain.get(1))).thenReturn(dto3);

        // when
        Mono<ResponseEntity<Flux<ProductDetail>>> result =
                controller.getProductProductIdSimilar("1", exchange); // usa tu operationId actual

        // then
        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
                    Assertions.assertNotNull(resp.getBody());
                    StepVerifier.create(resp.getBody())
                            .expectNext(dto2)
                            .expectNext(dto3)
                            .verifyComplete();
                })
                .verifyComplete();

        verify(useCase).handle("1");
        verify(mapper, times(2)).toDto(any());
    }
}
