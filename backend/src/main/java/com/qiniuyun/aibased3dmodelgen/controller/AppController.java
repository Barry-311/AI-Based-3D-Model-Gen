package com.qiniuyun.aibased3dmodelgen.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.qiniuyun.aibased3dmodelgen.common.BaseResponse;
import com.qiniuyun.aibased3dmodelgen.common.ResultUtils;
import com.qiniuyun.aibased3dmodelgen.constant.ObjectConstant;
import com.qiniuyun.aibased3dmodelgen.ai.AiGeneratorFacade;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.exception.ThrowUtils;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateResponse;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateStreamRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.TaskStatusResponse;
import com.qiniuyun.aibased3dmodelgen.model.entity.Model3D;
import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import com.qiniuyun.aibased3dmodelgen.model.vo.Model3DVO;
import com.qiniuyun.aibased3dmodelgen.service.AppService;
import com.qiniuyun.aibased3dmodelgen.service.ObjectDownloadService;
import com.qiniuyun.aibased3dmodelgen.service.Tripo3DService;
import com.qiniuyun.aibased3dmodelgen.service.Model3DService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/app")
@Slf4j
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private ObjectDownloadService objectDownloadService;

    @Resource
    private Tripo3DService tripo3DService;

    @Resource
    private Model3DService model3DService;

    @Resource
    private AiGeneratorFacade aiGeneratorFacade;

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile) {
        String result = appService.uploadPicture(multipartFile);
        return ResultUtils.success(result);
    }


    @GetMapping(value = "/augment/prompt", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> augmentPrompt(@RequestParam Long appId,
                                      @RequestParam String message) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");

        // 调用服务生成（SSE 流式返回）
        Flux<String> contentFlux = appService.augmentPrompt(appId, message, ObjectGenTypeEnum.PBR);
        return contentFlux
                .map(chunk -> {
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        // 发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    public void downloadObject(@PathVariable Long appId,
                                HttpServletResponse response) {
        // 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

        // 构建模型目录路径（生成目录）
        String sourceDirName = "Object_" + appId;
        String sourceDirPath = ObjectConstant.OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 检查目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "模型不存在，请先生成模型");
        String downloadFileName = appId + ".obj";
        // 调用通用下载服务
        objectDownloadService.downloadObject(sourceDirPath, downloadFileName, response);
    }

    /**
     * API端点：根据提示词创建模型生成任务
     * 请求体示例: { "prompt": "a high-quality, detailed, 3d model of a hamburger" }
     */
    @PostMapping("/generate")
    public Mono<ResponseEntity<ModelGenerateResponse>> createGenerationTask(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return tripo3DService.generateModelFromText(prompt)
                .map(ResponseEntity::ok) // 成功时返回 200 OK 和响应体
                .onErrorResume(e -> {
                    // 简单的错误处理
                    log.info("Error calling Tripo3D generate API: " + e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * API端点：查询指定任务的状态
     */
    @GetMapping("/status/{taskId}")
    public Mono<ResponseEntity<TaskStatusResponse>> getTaskStatus(@PathVariable String taskId) {
        return tripo3DService.checkTaskStatus(taskId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error calling Tripo3D status API: " + e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * 使用SSE实时推送3D模型生成进度，并保存模型数据
     */
    @PostMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Model3DVO>> generateModelWithPromptProgress(
            @RequestBody @Valid ModelGenerateStreamRequest request) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String prompt = request.getPrompt();
        
        return tripo3DService.generateModelFromText(prompt)
                .flatMapMany(response -> {
                    String taskId = response.getTaskId();
                    
                    // 创建轮询流，每5秒检查一次状态
                    return Flux.interval(Duration.ofSeconds(5))
                            .flatMap(tick -> tripo3DService.checkTaskStatus(taskId))
                            .map(statusResponse -> {
                                // 保存或更新模型数据
                                Model3D model3D = model3DService.saveOrUpdateModel(statusResponse);
                                
                                // 如果任务完成，异步下载模型文件
                                if ("success".equals(statusResponse.getStatus())) {
                                    // 异步下载，不阻塞响应
                                    Mono.fromRunnable(() -> model3DService.downloadAndSaveModel(model3D))
                                            .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                                            .subscribe();
                                }
                                
                                // 转换为VO对象
                                Model3DVO model3DVO = model3DService.getModel3DVO(model3D);
                                return model3DVO;
                            })
                            .takeUntil(model3DVO -> {
                                String status = model3DVO.getStatus();
                                return "success".equals(status) || "failed".equals(status) ||
                                       "banned".equals(status) || "expired".equals(status) || 
                                       "cancelled".equals(status);
                            })
                            .map(model3DVO -> ServerSentEvent.<Model3DVO>builder()
                                    .data(model3DVO)
                                    .event("progress")
                                    .build());
                })
                .onErrorResume(e -> {
                    log.error("Error in streaming generation: " + e.getMessage());
                    return Flux.just(ServerSentEvent.<Model3DVO>builder()
                            .event("error")
                            .data(null)
                            .build());
                });
    }



    /**
     * 使用SSE实时推送3D模型生成进度，并保存模型数据
     */
    @PostMapping(value = "/generate-stream-augmented", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Model3DVO>> generateModelWithAugmentedPromptProgress(
            @RequestBody @Valid ModelGenerateStreamRequest request) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        // 使用增强的 prompt
        String prompt = request.getPrompt();
        String augmentedPrompt = aiGeneratorFacade.generatePrompt(1L, prompt, ObjectGenTypeEnum.PBR);
        ThrowUtils.throwIf(augmentedPrompt == null, ErrorCode.SYSTEM_ERROR, "生成增强prompt失败");

        return tripo3DService.generateModelFromText(prompt)
                .flatMapMany(response -> {
                    String taskId = response.getTaskId();

                    // 创建轮询流，每5秒检查一次状态
                    return Flux.interval(Duration.ofSeconds(5))
                            .flatMap(tick -> tripo3DService.checkTaskStatus(taskId))
                            .map(statusResponse -> {
                                // 保存或更新模型数据
                                Model3D model3D = model3DService.saveOrUpdateModel(statusResponse);

                                // 如果任务完成，异步下载模型文件
                                if ("success".equals(statusResponse.getStatus())) {
                                    // 异步下载，不阻塞响应
                                    Mono.fromRunnable(() -> model3DService.downloadAndSaveModel(model3D))
                                            .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                                            .subscribe();
                                }

                                // 转换为VO对象
                                Model3DVO model3DVO = model3DService.getModel3DVO(model3D);
                                return model3DVO;
                            })
                            .takeUntil(model3DVO -> {
                                String status = model3DVO.getStatus();
                                return "success".equals(status) || "failed".equals(status) ||
                                        "banned".equals(status) || "expired".equals(status) ||
                                        "cancelled".equals(status);
                            })
                            .map(model3DVO -> ServerSentEvent.<Model3DVO>builder()
                                    .data(model3DVO)
                                    .event("progress")
                                    .build());
                })
                .onErrorResume(e -> {
                    log.error("Error in streaming generation: " + e.getMessage());
                    return Flux.just(ServerSentEvent.<Model3DVO>builder()
                            .event("error")
                            .data(null)
                            .build());
                });
    }


    /**
     * 使用SSE实时推送3D模型生成进度，并保存模型数据
     */
    @PostMapping(value = "/generate-stream-image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Model3DVO>> generateModelWithImageProgress(
            @RequestPart("file") MultipartFile picture) {
        // 参数校验
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        
        try {
            // 校验图片
            appService.validPicture(picture);
            // 获取图片类型
            String pictureType = appService.getPictureType(picture);
            // 上传图片到云存储
            String uploadedPictureUrl = appService.uploadPicture(picture);
            
            log.info("开始图片转模型任务，图片URL: {}, 类型: {}", uploadedPictureUrl, pictureType);
            
            // 请求 API 生成模型（现在使用优化后的上传流程）
            return tripo3DService.generateModelFromImage(uploadedPictureUrl, pictureType)
                    .flatMapMany(response -> {
                        String taskId = response.getTaskId();
                        log.info("图片转模型任务已创建，任务ID: {}", taskId);

                        // 创建轮询流，每5秒检查一次状态
                        return Flux.interval(Duration.ofSeconds(5))
                                .flatMap(tick -> tripo3DService.checkTaskStatus(taskId))
                                .map(statusResponse -> {
                                    // 保存或更新模型数据，使用专门的图片转模型方法
                                    Model3D model3D = model3DService.saveOrUpdateModelFromImage(statusResponse, uploadedPictureUrl);

                                    // 如果任务完成，异步下载模型文件
                                    if ("success".equals(statusResponse.getStatus())) {
                                        log.info("图片转模型任务完成，任务ID: {}", taskId);
                                        // 异步下载，不阻塞响应
                                        Mono.fromRunnable(() -> model3DService.downloadAndSaveModel(model3D))
                                                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                                                .subscribe(
                                                    null,
                                                    error -> log.error("模型文件下载失败，任务ID: {}, 错误: {}", taskId, error.getMessage())
                                                );
                                    } else if ("failed".equals(statusResponse.getStatus())) {
                                        log.warn("图片转模型任务失败，任务ID: {}", taskId);
                                    }

                                    // 转换为VO对象
                                    return model3DService.getModel3DVO(model3D);
                                })
                                .takeUntil(model3DVO -> {
                                    String status = model3DVO.getStatus();
                                    return "success".equals(status) || "failed".equals(status) ||
                                            "banned".equals(status) || "expired".equals(status) ||
                                            "cancelled".equals(status);
                                })
                                .map(model3DVO -> ServerSentEvent.<Model3DVO>builder()
                                        .data(model3DVO)
                                        .event("progress")
                                        .build());
                    })
                    .onErrorResume(e -> {
                        log.error("图片转模型流式生成过程中发生错误: {}", e.getMessage(), e);
                        // 创建错误响应的VO对象
                        Model3DVO errorVO = new Model3DVO();
                        errorVO.setStatus("failed");
                        errorVO.setProgress(0);
                        
                        return Flux.just(ServerSentEvent.<Model3DVO>builder()
                                .event("error")
                                .data(errorVO)
                                .build());
                    });
                    
        } catch (Exception e) {
            log.error("图片转模型请求处理失败: {}", e.getMessage(), e);
            // 对于同步异常，返回错误事件
            Model3DVO errorVO = new Model3DVO();
            errorVO.setStatus("failed");
            errorVO.setProgress(0);
            
            return Flux.just(ServerSentEvent.<Model3DVO>builder()
                    .event("error")
                    .data(errorVO)
                    .build());
        }
    }
}