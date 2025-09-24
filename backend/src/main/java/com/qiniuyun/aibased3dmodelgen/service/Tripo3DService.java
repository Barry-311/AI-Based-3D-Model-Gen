package com.qiniuyun.aibased3dmodelgen.service;

import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateResponse;
import com.qiniuyun.aibased3dmodelgen.model.dto.TaskStatusResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

import java.time.Duration;

@Service
@Slf4j
public class Tripo3DService {

    @Resource
    private WebClient webClient;

    // 从 application.properties 注入配置
    @Value("${tripo3d.api.key}")
    private String apiKey;

    @Autowired
    public Tripo3DService(WebClient tripo3dWebClient) {
        this.webClient = tripo3dWebClient;
    }

    /**
     * 根据提示词发起一个模型生成任务
     * @param prompt 提示词，例如 "a hamburger"
     * @return 包含任务ID的响应 Mono
     */
    public Mono<ModelGenerateResponse> generateModelFromText(String prompt) {
        ModelGenerateRequest requestBody = new ModelGenerateRequest("text_to_model", prompt);

        return this.webClient.post()
                .uri("/v2/openapi/task")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                        .map(errorBody -> {
                            log.error("Tripo3D API 4xx error: {}", errorBody);
                            return new RuntimeException("API请求错误: " + errorBody);
                        }))
                .onStatus(status -> status.is5xxServerError(), response -> Mono.just(new RuntimeException("Tripo3D服务器错误")))
                .bodyToMono(ModelGenerateResponse.class);
    }

    /**
     * 根据任务ID查询生成状态
     * @param taskId 任务ID
     * @return 包含任务状态和结果的响应 Mono
     */
    public Mono<TaskStatusResponse> checkTaskStatus(String taskId) {
        return this.webClient.get()
                .uri("/v2/openapi/task/{taskId}", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                        .map(errorBody -> {
                            log.error("Tripo3D API 4xx error: {}", errorBody);
                            return new RuntimeException("API请求错误: " + errorBody);
                        }))
                .onStatus(status -> status.is5xxServerError(), response -> Mono.just(new RuntimeException("Tripo3D服务器错误")))
                .bodyToMono(TaskStatusResponse.class);
    }

    /**
     * 下载模型文件
     * @param modelUrl 模型下载URL
     * @return 模型文件的字节流
     */
    public Mono<byte[]> downloadModel(String modelUrl) {
        return WebClient.create()
                .get()
                .uri(modelUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofMinutes(5)); // 设置超时时间
    }
}
