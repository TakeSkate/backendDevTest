package es.jgarci.similarproducts.infrastructure.adapter.output.http;

import es.jgarci.similarproducts.domain.exceptions.NotFoundException;
import es.jgarci.similarproducts.domain.exceptions.UpstreamException;
import es.jgarci.similarproducts.domain.model.Product;
import es.jgarci.similarproducts.domain.port.ExistingApisPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.time.Duration;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ExistingApisClient implements ExistingApisPort {

    private final WebClient webClient;

    @Override
    @Retry(name = "upstream")
    @CircuitBreaker(name = "upstream")
    public Mono<List<String>> findSimilarIds(String productId) {
        return webClient
                .get()
                .uri("/product/{id}/similarids", productId)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        resp -> {
                            if (resp.statusCode().value() == 404) {
                                return Mono.error(
                                        new NotFoundException("Product %s not found".formatted(productId)));
                            }
                            return Mono.error(
                                    new UpstreamException("4xx from similarids for %s".formatted(productId)));
                        })
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        resp ->
                                Mono.error(
                                        new UpstreamException("5xx from similarids for %s".formatted(productId))))
                .bodyToMono(new ParameterizedTypeReference<List<Integer>>() {
                })
                .map(list -> list.stream().map(String::valueOf).toList())
                .timeout(Duration.ofSeconds(2));
    }

    @Override
    @Retry(name = "upstream")
    @CircuitBreaker(name = "upstream")
    public Mono<Product> findProductById(String id) {
        return webClient
                .get()
                .uri("/product/{id}", id)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        resp -> {
                            if (resp.statusCode().value() == 404) {
                                return Mono.error(new NotFoundException("Product %s not found".formatted(id)));
                            }
                            return Mono.error(new UpstreamException("4xx from /product/%s".formatted(id)));
                        })
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new UpstreamException("5xx from /product/%s".formatted(id))))
                .bodyToMono(Product.class)
                .timeout(Duration.ofSeconds(2));
    }
}
