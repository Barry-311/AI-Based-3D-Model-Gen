package com.qiniuyun.aibased3dmodelgen.service;

import com.qiniuyun.aibased3dmodelgen.model.dto.ImageToModelRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateResponse;
import com.qiniuyun.aibased3dmodelgen.model.dto.TaskStatusResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.qiniuyun.aibased3dmodelgen.model.enums.ModelGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
        ModelGenerateRequest requestBody = new ModelGenerateRequest(ModelGenTypeEnum.TEXT.getValue(), prompt);

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
     * 上传图片并获取file_token
     * @param imageUrl 图片URL
     * @return 包含file_token的响应 Mono
     */
    public Mono<String> uploadImage(String imageUrl) {
        log.info("开始上传图片: {}", imageUrl);
        // 首先下载图片
        return this.webClient.get()
                .uri(imageUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .flatMap(imageBytes -> {
                    // 创建multipart请求上传图片
                    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
                    // 从URL中提取文件名和扩展名 - 使用final变量
                    final String fileName;
                    if (imageUrl.toLowerCase().contains(".webp")) {
                        fileName = "image.webp";
                    } else if (imageUrl.toLowerCase().contains(".png")) {
                        fileName = "image.png";
                    } else if (imageUrl.toLowerCase().contains(".jpg") || imageUrl.toLowerCase().contains(".jpeg")) {
                        fileName = "image.jpg";
                    } else {
                        fileName = "image.jpg"; // 默认值
                    }
                    // 创建文件资源
                    ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                        @Override
                        public String getFilename() {
                            return fileName;
                        }
                    };

                    parts.add("file", imageResource);

                    return this.webClient.post()
                            .uri("/v2/openapi/upload")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .bodyValue(parts)
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                                    .map(errorBody -> {
                                        log.error("❌ 图片上传4xx错误: {}", errorBody);
                                        return new RuntimeException("图片上传错误: " + errorBody);
                                    }))
                            .onStatus(status -> status.is5xxServerError(), response -> response.bodyToMono(String.class)
                                    .map(errorBody -> {
                                        log.error("❌ 图片上传5xx错误: {}", errorBody);
                                        return new RuntimeException("图片上传服务器错误: " + errorBody);
                                    }))
                            .bodyToMono(JsonNode.class)
                            .map(response -> {
                                log.info("图片上传响应: {}", response);
                                if (response.has("code") && response.get("code").asInt() == 0) {
                                    String imageToken = response.get("data").get("image_token").asText();
                                    log.info("✅ 图片上传成功，image_token: {}", imageToken);
                                    return imageToken;
                                } else {
                                    throw new RuntimeException("图片上传失败: " + response);
                                }
                            });
                })
                .doOnError(error -> log.error("❌ 图片上传失败: {}", error.getMessage(), error));
    }

    /**
     * 使用file_token生成模型
     * @param fileToken 上传后获得的文件token
     * @param imageType 图片类型
     * @return 包含任务ID的响应 Mono
     */
    public Mono<ModelGenerateResponse> generateModelFromImageToken(String fileToken, String imageType) {
        // 验证输入参数
        if (fileToken == null || fileToken.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("文件token不能为空"));
        }
        if (imageType == null || imageType.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("图片类型不能为空"));
        }

        // 创建图片文件信息
        ImageToModelRequest.FileInfo fileInfo = new ImageToModelRequest.FileInfo();
        fileInfo.setFile_token(fileToken.trim());
        fileInfo.setType(imageType.trim());

        // 创建图片转模型请求
        ImageToModelRequest requestBody = new ImageToModelRequest();
        requestBody.setFile(fileInfo);

        log.info("=== 发起图片转模型请求（使用file_token）===");

        return this.webClient.post()
                .uri("/v2/openapi/task")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                        .map(errorBody -> {
                            log.error("❌ Tripo3D API 4xx错误: {}", errorBody);
                            return new RuntimeException("API请求错误: " + errorBody);
                        }))
                .onStatus(status -> status.is5xxServerError(), response -> response.bodyToMono(String.class)
                        .map(errorBody -> {
                            log.error("❌ Tripo3D API 5xx错误: {}", errorBody);
                            return new RuntimeException("Tripo3D服务器错误: " + errorBody);
                        }))
                .bodyToMono(ModelGenerateResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> {
                    log.info("✅ 图片转模型请求成功");
                    log.info("响应码: {}", response.getCode());
                    log.info("响应消息: {}", response.getMessage());
                    if (response.getData() != null) {
                        log.info("任务ID: {}", response.getData().getTaskId());
                    }
                })
                .doOnError(error -> {
                    log.error("❌ 图片转模型请求失败: {}", error.getMessage(), error);
                });
    }

    /**
     * 修改原有的generateModelFromImage方法，使用正确的上传流程
     */
    public Mono<ModelGenerateResponse> generateModelFromImage(String imageUrl, String imageType) {
        return uploadImage(imageUrl)
                .flatMap(fileToken -> generateModelFromImageToken(fileToken, imageType));
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
