package com.qiniuyun.aibased3dmodelgen.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    // 从 application.properties 注入 Tripo3D 的基础 URL
    @Value("${tripo3d.api.base-url}")
    private String tripo3dBaseUrl;

    @Bean
    public WebClient tripo3dWebClient() {
        // 配置连接池
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        // 配置HttpClient，增加超时时间和重试机制
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofMinutes(5))  // 响应超时5分钟
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)  // 连接超时30秒
                .resolver(spec -> spec.queryTimeout(Duration.ofSeconds(10)));  // DNS查询超时10秒

        // 配置更大的缓冲区大小（50MB）
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(tripo3dBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }

}
