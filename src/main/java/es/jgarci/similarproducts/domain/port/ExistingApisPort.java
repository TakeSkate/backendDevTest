package es.jgarci.similarproducts.domain.port;

import es.jgarci.similarproducts.domain.model.Product;

import java.util.List;

import reactor.core.publisher.Mono;

public interface ExistingApisPort {
    Mono<List<String>> findSimilarIds(String productId);

    Mono<Product> findProductById(String productId);
}
