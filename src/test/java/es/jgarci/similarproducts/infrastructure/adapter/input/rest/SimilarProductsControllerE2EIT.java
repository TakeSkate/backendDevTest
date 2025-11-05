package es.jgarci.similarproducts.infrastructure.adapter.input.rest;

import com.github.tomakehurst.wiremock.WireMockServer;
import es.jgarci.similarproducts.api.model.ProductDetail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import es.jgarci.similarproducts.infrastructure.config.SimilarProductsProps;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SimilarProductsControllerE2EIT {
    @Autowired
    WebTestClient webTestClient;

    // WireMock como “upstream”
    static WireMockServer wiremock = new WireMockServer(options().dynamicPort());

    @BeforeAll
    static void start() {
        wiremock.start();
    }

    @AfterAll
    static void stop() {
        wiremock.stop();
    }

    // Exponemos la URL para la TestConfiguration
    @DynamicPropertySource
    static void reg(DynamicPropertyRegistry r) {
        r.add("wiremock.base-url", () -> wiremock.baseUrl());
    }

    @MockitoBean
    SimilarProductsProps props;

    @BeforeEach
    void setupProps() {
        when(props.timeout()).thenReturn(Duration.ofSeconds(1));
        when(props.concurrency()).thenReturn(4);
    }

    @TestConfiguration
    static class TestWebClientConfig {
        @Bean
        @Primary
        WebClient webClient(@Value("${wiremock.base-url}") String baseUrl) {
            return WebClient.builder().baseUrl(baseUrl).build();
        }
    }

    @Test
    void e2e_returnsFluxOfProductDetail() {
        // /product/{id}/similarids -> devuelve 3 ids
        wiremock.stubFor(get(urlEqualTo("/product/123/similarids"))
                .willReturn(okJson("[101,102,103]")));

        // /product/{id} -> 2 OK + 1 NOT FOUND (se filtrará por onErrorResume en el servicio)
        wiremock.stubFor(get(urlEqualTo("/product/101"))
                .willReturn(okJson("""
                          {"id":"101","name":"A","price":10.00,"availability":true}
                        """)));
        wiremock.stubFor(get(urlEqualTo("/product/102"))
                .willReturn(notFound()));
        wiremock.stubFor(get(urlEqualTo("/product/103"))
                .willReturn(okJson("""
                          {"id":"103","name":"C","price":30.50,"availability":false}
                        """)));

        webTestClient.get()
                .uri("/product/{id}/similar", "123")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductDetail.class)
                .value(list -> {
                    var ids = list.stream().map(ProductDetail::getId).toList();
                    assertThat(ids, containsInAnyOrder("101", "103"));
                });
    }
}
