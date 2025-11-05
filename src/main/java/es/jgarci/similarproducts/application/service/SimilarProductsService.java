package es.jgarci.similarproducts.application.service;

import es.jgarci.similarproducts.domain.model.Product;
import es.jgarci.similarproducts.domain.port.ExistingApisPort;
import es.jgarci.similarproducts.infrastructure.config.SimilarProductsProps;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SimilarProductsService implements SimilarProductsUseCase {

    private final ExistingApisPort existingApisClient;
    private final SimilarProductsProps props;

    @Override
    public Mono<List<Product>> handle(String productId) {
        return existingApisClient
                .findSimilarIds(productId)
                .flatMapMany(Flux::fromIterable)
                .flatMap(
                        id ->
                                existingApisClient
                                        .findProductById(id)
                                        .timeout(props.timeout())
                                        .onErrorResume(e -> Mono.empty()),
                        props.concurrency())
                .collectList();
    }
}
