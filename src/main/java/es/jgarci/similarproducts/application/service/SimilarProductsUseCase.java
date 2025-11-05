package es.jgarci.similarproducts.application.service;

import es.jgarci.similarproducts.domain.model.Product;

import java.util.List;

import reactor.core.publisher.Mono;

public interface SimilarProductsUseCase {
    Mono<List<Product>> handle(String productId);
}
