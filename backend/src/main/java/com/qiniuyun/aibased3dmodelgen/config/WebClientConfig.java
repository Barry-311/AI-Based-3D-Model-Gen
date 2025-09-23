package com.qiniuyun.aibased3dmodelgen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // 从 application.properties 注入 Tripo3D 的基础 URL
    @Value("${tripo3d.api.base-url}")
    private String tripo3dBaseUrl;

    @Bean
    public WebClient tripo3dWebClient() {
        return WebClient.builder()
                .baseUrl(tripo3dBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
