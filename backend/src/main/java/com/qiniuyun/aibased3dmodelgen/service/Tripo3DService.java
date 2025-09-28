package com.qiniuyun.aibased3dmodelgen.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.qiniuyun.aibased3dmodelgen.exception.BusinessException;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.model.dto.ImageToModelRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateResponse;
import com.qiniuyun.aibased3dmodelgen.model.dto.TaskStatusResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.qiniuyun.aibased3dmodelgen.model.enums.ModelGenTypeEnum;
import com.qiniuyun.aibased3dmodelgen.model.enums.UploadFileTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class Tripo3DService {

    @Resource
    private WebClient webClient;

    @Resource
    @Lazy
    private AppService appService;

    // 从 application.properties 注入配置
    @Value("${tripo3d.api.key}")
    private String apiKey;

    @Autowired
    public Tripo3DService(WebClient tripo3dWebClient) {
        this.webClient = tripo3dWebClient;
    }

    /**
     * 根据提示词发起一个模型生成任务
     * @param modelGenerateRequest 包含提示词的请求
     * @return 包含任务ID的响应 Mono
     */
    public Mono<ModelGenerateResponse> generateModelFromText(ModelGenerateRequest modelGenerateRequest) {
        modelGenerateRequest.setType(ModelGenTypeEnum.TEXT.getValue());

        return this.webClient.post()
                .uri("/v2/openapi/task")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(modelGenerateRequest)
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
    public Mono<ModelGenerateResponse> generateModelFromImageToken(String fileToken, String imageType, ImageToModelRequest request) {
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
        request.setType(ModelGenTypeEnum.IMAGE.getValue());
        // 创建图片转模型请求
        request.setFile(fileInfo);

        log.info("=== 发起图片转模型请求（使用file_token）===");

        return this.webClient.post()
                .uri("/v2/openapi/task")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(request)
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
    public Mono<ModelGenerateResponse> generateModelFromImage(String imageUrl, String imageType, ImageToModelRequest requestBody) {
        return uploadImage(imageUrl)
                .flatMap(fileToken -> generateModelFromImageToken(fileToken, imageType, requestBody));
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
     * [新增] 下载模型和预览图，上传到COS，然后返回COS的URL
     * @param renderedImageUrl 模型渲染图片URL
     * @param pbrModelUrl 模型下载URL
     * @return 包含COS URL的DTO或Map
     */
    public AssetUrls downloadAndUploadAssets(String renderedImageUrl, String pbrModelUrl) {
        // 用于存放本次操作创建的临时文件，以便在最后统一清理
        List<File> tempFiles = new ArrayList<>();

        try {
            // 1. 处理预览图
            log.info("开始处理预览图: {}", renderedImageUrl);
            // 生成一个唯一的文件名，避免冲突
            String imageFileName = UUID.randomUUID() + ".webp";
            File tempImageFile = downloadUrlToTempFile(renderedImageUrl, "image_", imageFileName);
            tempFiles.add(tempImageFile);

            // 上传到COS
            String imageUrlOnCos = appService.uploadFile(tempImageFile, imageFileName, UploadFileTypeEnum.RENDERED_IMAGE);

            // 2. 处理PBR模型
            log.info("开始处理PBR模型: {}", pbrModelUrl);
            String modelFileName = UUID.randomUUID() + ".glb";
            File tempModelFile = downloadUrlToTempFile(pbrModelUrl, "model_", modelFileName);
            tempFiles.add(tempModelFile);

            // 上传到COS
            String modelUrlOnCos = appService.uploadFile(tempModelFile, modelFileName, UploadFileTypeEnum.PBR_MODEL);

            // 3. 返回结果
            return new AssetUrls(imageUrlOnCos, modelUrlOnCos);

        } catch (Exception e) {
            log.error("下载并上传资源时发生错误. ImageURL: {}, ModelURL: {}", renderedImageUrl, pbrModelUrl, e);
            // 抛出异常，让上层业务（比如SSE流）能捕获到失败
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "处理模型文件失败");
        } finally {
            // 4. 清理所有临时文件，无论成功还是失败
            log.info("开始清理临时文件...");
            for (File tempFile : tempFiles) {
                if (tempFile != null && tempFile.exists()) {
                    if (!tempFile.delete()) {
                        log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                    }
                }
            }
            log.info("临时文件清理完毕.");
        }
    }

    /**
     * [新增] 辅助方法：根据URL下载内容并保存到临时文件
     * @param url 文件URL
     * @param prefix 临时文件前缀
     * @param fileName 临时文件名
     * @return 创建的临时文件对象
     */
    private File downloadUrlToTempFile(String url, String prefix, String fileName) {
        try {
            byte[] fileBytes = HttpUtil.downloadBytes(url);
            // 使用File.createTempFile更安全，它会在系统默认的临时目录中创建文件
            File tempFile = File.createTempFile(prefix, fileName);
            FileUtil.writeBytes(fileBytes, tempFile);
            log.info("文件已成功下载到临时位置: {}", tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception e) {
            log.error("从URL下载文件失败: {}", url, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载文件失败");
        }
    }

    /**
     * [新增] 一个简单的DTO类来封装返回的两个URL，比Map更清晰
     * 可以作为TripoService的内部静态类，或者一个独立的DTO类
     */
    public static class AssetUrls {
        private final String imageUrl;
        private final String modelUrl;

        public AssetUrls(String imageUrl, String modelUrl) {
            this.imageUrl = imageUrl;
            this.modelUrl = modelUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getModelUrl() {
            return modelUrl;
        }
    }
}
