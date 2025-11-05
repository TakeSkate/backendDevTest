package es.jgarci.similarproducts.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class ExistingApisWebClientConfig {


    @Bean
    public WebClient existingApisWebClient(ExistingApisProps existingApisProps) {
        var provider =
                ConnectionProvider.builder("http").maxConnections(200).pendingAcquireMaxCount(500).build();

        var httpClient = HttpClient.create(provider).responseTimeout(existingApisProps.httpTimeout());

        return WebClient.builder()
                .baseUrl(existingApisProps.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                                .build())
                .build();
    }
}
