package es.jgarci.similarproducts.domain.model;

import java.math.BigDecimal;

public record Product(String id, String name, BigDecimal price, Boolean availability) {
}
