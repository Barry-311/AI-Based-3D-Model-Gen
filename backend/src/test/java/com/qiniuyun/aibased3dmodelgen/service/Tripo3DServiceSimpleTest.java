package com.qiniuyun.aibased3dmodelgen.service;

import com.qiniuyun.aibased3dmodelgen.model.dto.ImageToModelRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateResponse;
import com.qiniuyun.aibased3dmodelgen.model.dto.TaskStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tripo3D服务简单测试
 * 直接使用application.yml中配置的API密钥
 */
@SpringBootTest
@Slf4j
class Tripo3DServiceSimpleTest {

    @Autowired
    private Tripo3DService tripo3DService;

    @Value("${tripo3d.api.key}")
    private String apiKey;

    /**
     * 测试API密钥是否正确配置
     */
    @Test
    void testApiKeyConfiguration() {
        assertNotNull(apiKey, "API密钥不能为空");
        assertTrue(apiKey.startsWith("tsk_"), "API密钥格式应该以tsk_开头");
        log.info("API密钥配置正确: {}", apiKey.substring(0, 8) + "...");
    }

    /**
     * 测试文本生成3D模型
     * 只有当API密钥不是默认值时才运行
     */
    @Test
    void testGenerateModelFromText() {
        // 跳过测试如果API密钥是默认值
        if (apiKey.equals("tsk_your_actual_api_key_here")) {
            log.info("跳过真实API测试 - 请在application.yml中配置真实的API密钥");
            return;
        }

        String prompt = "a simple red cube";
        
        log.info("开始测试API调用 - 生成模型");
        ModelGenerateRequest modelGenerateRequest = new ModelGenerateRequest();
        modelGenerateRequest.setPrompt(prompt);
        modelGenerateRequest.setStyle("gold");
        modelGenerateRequest.setTexture(false);
        modelGenerateRequest.setModel_seed(0);
        modelGenerateRequest.setTexture_quality("low");
        Mono<ModelGenerateResponse> responseMono = tripo3DService.generateModelFromText(modelGenerateRequest);
        
        StepVerifier.create(responseMono)
                .expectNextMatches(response -> {
                    log.info("收到响应: code={}, taskId={}", response.getCode(), response.getTaskId());
                    
                    // 验证响应结构
                    assertNotNull(response, "响应不能为空");
                    assertEquals(0, response.getCode(), "API调用应该成功");
                    assertNotNull(response.getTaskId(), "任务ID不能为空");
                    assertTrue(response.getTaskId().length() > 0, "任务ID不能为空字符串");
                    
                    return true;
                })
                .verifyComplete();
    }

    /**
     * 测试查询任务状态
     */
    @Test
    void testCheckTaskStatus() {
        // 跳过测试如果API密钥是默认值
        if (apiKey.equals("tsk_your_actual_api_key_here")) {
            log.info("跳过真实API测试 - 请在application.yml中配置真实的API密钥");
            return;
        }

        String prompt = "a small wooden chair";
        
        log.info("开始测试API调用 - 完整流程");
        ModelGenerateRequest modelGenerateRequest = new ModelGenerateRequest();
        modelGenerateRequest.setPrompt(prompt);
        modelGenerateRequest.setModel_seed(11);
        modelGenerateRequest.setTexture_seed(22);
        modelGenerateRequest.setTexture_quality("detailed");
        modelGenerateRequest.setGeometry_quality("detailed");
        modelGenerateRequest.setStyle("gold");
        modelGenerateRequest.setTexture(false);
        modelGenerateRequest.setFace_limit(1000);
        modelGenerateRequest.setAuto_size(false);
        modelGenerateRequest.setCompress("");

        // 第一步：创建任务
        Mono<String> taskIdMono = tripo3DService.generateModelFromText(modelGenerateRequest)
                .map(ModelGenerateResponse::getTaskId);
        
        // 第二步：查询任务状态
        Mono<TaskStatusResponse> statusMono = taskIdMono
                .flatMap(taskId -> {
                    log.info("任务创建成功，ID: {}", taskId);
                    // 等待一秒后查询状态
                    return Mono.delay(Duration.ofSeconds(1))
                            .then(tripo3DService.checkTaskStatus(taskId));
                });
        
        StepVerifier.create(statusMono)
                .expectNextMatches(response -> {
                    log.info("任务状态: {}, 进度: {}%", response.getStatus(), response.getProgress());
                    
                    // 验证响应结构
                    assertNotNull(response, "状态响应不能为空");
                    assertEquals(0, response.getCode(), "状态查询应该成功");
                    assertNotNull(response.getStatus(), "任务状态不能为空");
                    assertTrue(response.getProgress() >= 0 && response.getProgress() <= 100, "进度应该在0-100之间");
                    
                    return true;
                })
                .verifyComplete();
    }


    /**
     * 限制轮询次数的轮询方法
     */
    private Mono<TaskStatusResponse> pollTaskWithLimit(String taskId, int maxAttempts) {
        return pollTaskWithLimit(taskId, maxAttempts, 1);
    }

    private Mono<TaskStatusResponse> pollTaskWithLimit(String taskId, int maxAttempts, int currentAttempt) {
        return tripo3DService.checkTaskStatus(taskId)
                .flatMap(response -> {
                    String status = response.getStatus();
                    log.info("第{}次轮询 - 状态: {}, 进度: {}%", currentAttempt, status, response.getProgress());

                    if ("success".equals(status) || "failed".equals(status) ||
                        "banned".equals(status) || "expired".equals(status) ||
                        "cancelled".equals(status) || currentAttempt >= maxAttempts) {
                        return Mono.just(response);
                    }

                    // 继续轮询
                    return Mono.delay(Duration.ofSeconds(3))
                            .then(pollTaskWithLimit(taskId, maxAttempts, currentAttempt + 1));
                });
    }


    @Test
    void testCompleteImageToModelWorkflowWithUpload() {
        // 跳过测试如果API密钥是默认值
        if ("your_api_key_here".equals(apiKey) || "tsk_your_actual_api_key_here".equals(apiKey)) {
            log.info("跳过使用上传流程的完整图片转模型工作流测试 - 使用默认API密钥");
            return;
        }

        // 使用官方示例图片
        String testImageUrl = "https://platform.tripo3d.ai/assets/front-235queJB.jpg";
        String imageType = "image/jpeg";

        log.info("开始测试使用上传流程的完整图片转模型工作流");
        log.info("图片URL: {}", testImageUrl);

        ImageToModelRequest imageToModelRequest = new ImageToModelRequest();
        imageToModelRequest.setTexture(false);
        imageToModelRequest.setTexture_alignment("original_image");
        imageToModelRequest.setModel_seed(1);
        imageToModelRequest.setTexture_seed(2);
        imageToModelRequest.setTexture_quality("standard");
        imageToModelRequest.setGeometry_quality("standard");
        imageToModelRequest.setFace_limit(1234);
        imageToModelRequest.setAuto_size(false);
        imageToModelRequest.setCompress("");

        Mono<TaskStatusResponse> completedTaskMono = tripo3DService.generateModelFromImage(testImageUrl, imageType, imageToModelRequest)
                .doOnNext(response -> {
                    log.info("图片转模型请求已提交，任务ID: {}", response.getData().getTaskId());
                    if (response.getCode() != 0) {
                        throw new RuntimeException("图片转模型请求失败: " + response.getMessage());
                    }
                })
                .flatMap(response -> {
                    String taskId = response.getData().getTaskId();
                    // 轮询等待任务完成
                    return pollTaskWithLimit(taskId, 100); // 最多轮询30次，约15分钟
                })
                .timeout(Duration.ofMinutes(20)); // 设置20分钟超时

        StepVerifier.create(completedTaskMono)
                .expectNextMatches(response -> {
                    log.info("=== 使用上传流程的图片转模型任务完成 ===");
                    log.info("最终状态: {}", response.getStatus());
                    log.info("进度: {}%", response.getProgress());
                    log.info("完整响应: {}", response);

                    if ("failed".equals(response.getStatus())) {
                        log.error("❌ 任务失败");
                        if (response.getData() != null && response.getData().getOutput() != null) {
                            log.error("失败原因: {}", response.getData().getOutput());
                        }
                        return false; // 任务失败
                    }

                    if (!"success".equals(response.getStatus())) {
                        log.warn("⚠️ 任务未成功完成，状态: {}", response.getStatus());
                        return false;
                    }

                    // 验证任务成功完成
                    assertEquals("success", response.getStatus(), "任务应该成功完成");
                    assertEquals(100, response.getProgress(), "进度应该是100%");

                    // 检查输出结果
                    assertNotNull(response.getData(), "任务数据不能为空");
                    log.info("任务数据: {}", response.getData());

                    if (response.getData().getOutput() != null) {
                        log.info("✅ 输出结果: {}", response.getData().getOutput());
                        
                        String modelUrl = response.getData().getOutput().getModel();
                        if (modelUrl != null && !modelUrl.isEmpty()) {
                            log.info("📦 模型下载URL: {}", modelUrl);
                        }
                        
                        String renderedImageUrl = response.getData().getOutput().getRenderedImage();
                        if (renderedImageUrl != null && !renderedImageUrl.isEmpty()) {
                            log.info("🖼️ 渲染图片URL: {}", renderedImageUrl);
                        }
                    } else {
                        log.warn("⚠️ 输出结果为空");
                    }

                    return true;
                })
                .verifyComplete();
    }
}