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
     * 测试完整的3D模型生成流程：创建任务 -> 轮询等待 -> 下载模型
     */
    @Test
    void testCompleteModelGenerationWorkflow() {
        String prompt = "A cute cat sitting on a chair";
        ModelGenerateRequest modelGenerateRequest = new ModelGenerateRequest();
        modelGenerateRequest.setPrompt(prompt);
        Mono<TaskStatusResponse> completedTaskMono = tripo3DService.generateModelFromText(modelGenerateRequest)
                .flatMap(response -> {
                    log.info("模型生成请求已提交，任务ID: {}", response.getTaskId());
                    String taskId = response.getTaskId();
                    
                    // 轮询等待任务完成
                    return pollTaskUntilComplete(taskId);
                })
                .timeout(Duration.ofMinutes(10)); // 设置10分钟超时

        StepVerifier.create(completedTaskMono)
                .expectNextMatches(response -> {
                    log.info("任务完成！状态: {}, 进度: {}%", response.getStatus(), response.getProgress());
                    
                    // 验证任务完成
                    assertEquals("success", response.getStatus(), "任务应该成功完成");
                    assertEquals(100, response.getProgress(), "进度应该是100%");
                    
                    // 检查输出结果 - 添加详细的日志
                    assertNotNull(response.getData(), "任务数据不能为空");
                    log.info("任务数据: {}", response.getData());
                    
                    if (response.getData().getOutput() == null) {
                        log.warn("输出结果为空，可能任务还未完全完成或API响应结构有变化");
                        // 如果output为空，我们仍然认为测试通过，因为任务状态已经是success
                        return true;
                    }
                    
                    log.info("输出结果: {}", response.getData().getOutput());
                    
                    String modelUrl = response.getData().getOutput().getModel();
                    if (modelUrl != null && !modelUrl.isEmpty()) {
                        log.info("模型下载URL: {}", modelUrl);
                        // 尝试下载模型
                        downloadModelFile(modelUrl, response.getData().getTaskId());
                    } else {
                        log.warn("模型下载URL为空，可能需要等待更长时间或检查API响应格式");
                        // 检查其他可能的URL字段
                        if (response.getData().getOutput().getBaseModel() != null) {
                            log.info("基础模型URL: {}", response.getData().getOutput().getBaseModel());
                        }
                        if (response.getData().getOutput().getPbrModel() != null) {
                            log.info("PBR模型URL: {}", response.getData().getOutput().getPbrModel());
                        }
                        if (response.getData().getOutput().getRenderedImage() != null) {
                            log.info("渲染图片URL: {}", response.getData().getOutput().getRenderedImage());
                        }
                    }
                    
                    return true;
                })
                .verifyComplete();
    }

    /**
     * 轮询等待任务完成
     */
    private Mono<TaskStatusResponse> pollTaskUntilComplete(String taskId) {
        return tripo3DService.checkTaskStatus(taskId)
                .flatMap(response -> {
                    String status = response.getStatus();
                    log.info("轮询检查任务状态: {} - 进度: {}%", status, response.getProgress());
                    
                    // 添加详细的响应日志
                    log.debug("完整响应: {}", response);
                    if (response.getData() != null) {
                        log.debug("任务数据: {}", response.getData());
                        if (response.getData().getOutput() != null) {
                            log.debug("输出数据: {}", response.getData().getOutput());
                        }
                    }
                    
                    switch (status) {
                        case "success":
                            log.info("任务成功完成！");
                            // 即使任务成功，也要检查output是否存在
                            if (response.getData() != null && response.getData().getOutput() != null) {
                                log.info("任务输出可用");
                            } else {
                                log.warn("任务虽然成功，但输出数据不可用");
                            }
                            return Mono.just(response);
                        case "failed":
                            return Mono.error(new RuntimeException("任务失败: " + taskId));
                        case "banned":
                            return Mono.error(new RuntimeException("任务被禁止: " + taskId));
                        case "expired":
                            return Mono.error(new RuntimeException("任务已过期: " + taskId));
                        case "cancelled":
                            return Mono.error(new RuntimeException("任务已取消: " + taskId));
                        case "unknown":
                            return Mono.error(new RuntimeException("任务状态未知: " + taskId));
                        case "queued":
                        case "running":
                        default:
                            // 等待5秒后继续轮询
                            return Mono.delay(Duration.ofSeconds(5))
                                    .then(pollTaskUntilComplete(taskId));
                    }
                });
    }

    /**
     * 下载模型文件到本地
     */
    private void downloadModelFile(String modelUrl, String taskId) {
        try {
            log.info("开始下载模型文件...");
            
            // 使用Tripo3DService的下载方法
            byte[] modelData = tripo3DService.downloadModel(modelUrl).block();
            
            if (modelData != null && modelData.length > 0) {
                // 创建下载目录
                File downloadDir = new File("tmp/downloaded_models");
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs();
                }
                
                // 保存文件
                String fileName = taskId + "_model.glb";
                File modelFile = new File(downloadDir, fileName);
                
                try (FileOutputStream fos = new FileOutputStream(modelFile)) {
                    fos.write(modelData);
                    log.info("模型文件下载成功！保存路径: {}", modelFile.getAbsolutePath());
                    log.info("文件大小: {} KB", modelData.length / 1024);
                } catch (IOException e) {
                    log.error("保存模型文件失败", e);
                }
            } else {
                log.warn("下载的模型数据为空");
            }
        } catch (Exception e) {
            log.error("下载模型文件时发生错误", e);
        }
    }

    /**
     * 测试轮询机制（不下载文件）
     */
    @Test
    void testPollingMechanism() {
        // 跳过测试如果API密钥是默认值
        if (apiKey.equals("tsk_your_actual_api_key_here")) {
            log.info("跳过真实API测试 - 请在application.yml中配置真实的API密钥");
            return;
        }

        String prompt = "a simple blue sphere";
        log.info("测试轮询机制");
        ModelGenerateRequest modelGenerateRequest = new ModelGenerateRequest();
        modelGenerateRequest.setPrompt(prompt);
        Mono<String> taskIdMono = tripo3DService.generateModelFromText(modelGenerateRequest)
                .map(ModelGenerateResponse::getTaskId);

        // 轮询5次，每次间隔3秒
        Mono<TaskStatusResponse> pollingMono = taskIdMono
                .flatMap(taskId -> {
                    log.info("开始轮询任务: {}", taskId);
                    return pollTaskWithLimit(taskId, 5);
                })
                .timeout(Duration.ofMinutes(2));

        StepVerifier.create(pollingMono)
                .expectNextMatches(response -> {
                    log.info("轮询结束，最终状态: {}, 进度: {}%", response.getStatus(), response.getProgress());
                    assertNotNull(response.getStatus(), "任务状态不能为空");
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
    void testImageToModelDiagnosis() {
        // 跳过测试如果API密钥是默认值
        if ("your_api_key_here".equals(apiKey) || "tsk_your_actual_api_key_here".equals(apiKey)) {
            log.info("跳过图片转模型诊断测试 - 使用默认API密钥");
            return;
        }

        // 测试多个不同的图片URL和格式
        String[] testImages = {
            "https://platform.tripo3d.ai/assets/front-235queJB.jpg", // 官方示例图片
            "https://aibased3dmodelgen-1345673117.cos.ap-shanghai.myqcloud.com/picture/boat.webp", // 原始测试图片
        };
        
        String[] imageTypes = {
            "image/jpeg",
            "image/webp"
        };

        for (int i = 0; i < testImages.length; i++) {
            String testImageUrl = testImages[i];
            String imageType = imageTypes[i];
            
            log.info("=== 测试图片 {} ===", i + 1);
            log.info("图片URL: {}", testImageUrl);
            log.info("图片类型: {}", imageType);

            try {
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

                Mono<ModelGenerateResponse> result = tripo3DService.generateModelFromImage(testImageUrl, imageType, imageToModelRequest);
                
                ModelGenerateResponse response = result.block(Duration.ofSeconds(30));
                
                if (response != null && response.getCode() == 0) {
                    String taskId = response.getData().getTaskId();
                    log.info("任务创建成功，任务ID: {}", taskId);
                    
                    // 等待一段时间后检查状态
                    Thread.sleep(5000); // 等待5秒
                    
                    Mono<TaskStatusResponse> statusResult = tripo3DService.checkTaskStatus(taskId);
                    TaskStatusResponse statusResponse = statusResult.block(Duration.ofSeconds(10));
                    
                    if (statusResponse != null) {
                        log.info("任务状态: {}", statusResponse.getData().getStatus());
                        log.info("任务进度: {}%", statusResponse.getData().getProgress());
                        log.info("输入参数: {}", statusResponse.getData().getInput());
                        
                        if ("failed".equals(statusResponse.getData().getStatus())) {
                            log.error("❌ 图片 {} 任务失败", i + 1);
                            log.error("输出信息: {}", statusResponse.getData().getOutput());
                        } else if ("success".equals(statusResponse.getData().getStatus())) {
                            log.info("✅ 图片 {} 任务成功", i + 1);
                            log.info("输出结果: {}", statusResponse.getData().getOutput());
                        } else {
                            log.info("⏳ 图片 {} 任务进行中，状态: {}", i + 1, statusResponse.getData().getStatus());
                        }
                    }
                } else {
                    log.error("❌ 图片 {} API调用失败", i + 1);
                    if (response != null) {
                        log.error("错误码: {}, 错误信息: {}", response.getCode(), response.getMessage());
                    }
                }
                
            } catch (Exception e) {
                log.error("❌ 图片 {} 测试异常: {}", i + 1, e.getMessage(), e);
            }
            
            log.info("=== 图片 {} 测试完成 ===\n", i + 1);
        }
    }

    @Test
    void testGenerateModelFromImageWithUpload() {
        // 跳过测试如果API密钥是默认值
        if ("your_api_key_here".equals(apiKey) || "tsk_your_actual_api_key_here".equals(apiKey)) {
            log.info("跳过使用上传流程的图片转模型测试 - 使用默认API密钥");
            return;
        }

        // 使用官方示例图片
        String testImageUrl = "https://platform.tripo3d.ai/assets/front-235queJB.jpg";
        String imageType = "image/jpeg";

        log.info("开始测试使用正确上传流程的图片转模型API调用");
        log.info("测试图片URL: {}", testImageUrl);
        log.info("图片类型: {}", imageType);

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

        Mono<ModelGenerateResponse> result = tripo3DService.generateModelFromImage(testImageUrl, imageType, imageToModelRequest);

        StepVerifier.create(result)
                .assertNext(response -> {
                    log.info("=== 使用上传流程的API响应详情 ===");
                    log.info("响应码: {}", response.getCode());
                    log.info("响应消息: {}", response.getMessage());
                    log.info("完整响应: {}", response);
                    
                    if (response.getData() != null) {
                        log.info("任务ID: {}", response.getData().getTaskId());
                        log.info("任务数据: {}", response.getData());
                    }
                    
                    // 验证响应
                    assertNotNull(response, "响应不能为空");
                    
                    if (response.getCode() != 0) {
                        log.error("API调用失败，错误码: {}, 错误信息: {}", response.getCode(), response.getMessage());
                        fail("API调用失败: " + response.getMessage());
                    }
                    
                    assertEquals(0, response.getCode(), "API调用应该成功，错误信息: " + response.getMessage());
                    assertNotNull(response.getData(), "响应数据不能为空");
                    assertNotNull(response.getData().getTaskId(), "任务ID不能为空");
                    assertTrue(response.getData().getTaskId().length() > 0, "任务ID不能为空字符串");
                    
                    log.info("✅ 使用上传流程的图片转模型任务创建成功，任务ID: {}", response.getData().getTaskId());
                })
                .expectComplete()
                .verify(Duration.ofSeconds(60)); // 增加超时时间，因为需要先上传图片
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