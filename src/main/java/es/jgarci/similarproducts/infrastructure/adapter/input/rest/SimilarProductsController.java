package es.jgarci.similarproducts.infrastructure.adapter.input.rest;

import es.jgarci.similarproducts.api.SimilarProductsApi;
import es.jgarci.similarproducts.api.model.ProductDetail;
import es.jgarci.similarproducts.application.service.SimilarProductsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class SimilarProductsController implements SimilarProductsApi {

    private final SimilarProductsUseCase similarProductsService;
    private final ProductMapper productMapper;

    @Override
    public Mono<ResponseEntity<Flux<ProductDetail>>> getProductProductIdSimilar(
            String productId, ServerWebExchange exchange) {
        Flux<ProductDetail> body =
                similarProductsService
                        .handle(productId)
                        .flatMapMany(Flux::fromIterable)
                        .map(productMapper::toDto);

        return Mono.just(ResponseEntity.ok(body));
    }
}
