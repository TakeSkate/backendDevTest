package es.jgarci.similarproducts.infrastructure.mapper;

import es.jgarci.similarproducts.api.model.ProductDetail;
import es.jgarci.similarproducts.domain.model.Product;
import es.jgarci.similarproducts.infrastructure.adapter.input.rest.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapsDomainToDto() {
        Product product = new Product("2", "Dress", new BigDecimal("19.99"), true);

        ProductDetail productDetail = mapper.toDto(product);

        assertThat(productDetail.getId()).isEqualTo("2");
        assertThat(productDetail.getName()).isEqualTo("Dress");
        assertThat(productDetail.getPrice()).isEqualByComparingTo("19.99");
        assertThat(productDetail.getAvailability()).isTrue();
    }
}
