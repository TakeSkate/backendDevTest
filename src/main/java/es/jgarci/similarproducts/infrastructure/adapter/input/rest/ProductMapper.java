package es.jgarci.similarproducts.infrastructure.adapter.input.rest;

import es.jgarci.similarproducts.api.model.ProductDetail;
import es.jgarci.similarproducts.domain.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDetail toDto(Product product);
}
