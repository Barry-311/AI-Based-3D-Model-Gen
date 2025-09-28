package com.qiniuyun.aibased3dmodelgen.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.qiniuyun.aibased3dmodelgen.ai.AiGeneratorFacade;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.exception.ThrowUtils;
import com.qiniuyun.aibased3dmodelgen.model.dto.ImageGenerateStreamRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.ImageToModelRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateStreamRequest;
import com.qiniuyun.aibased3dmodelgen.model.entity.Model3D;
import com.qiniuyun.aibased3dmodelgen.model.enums.UploadFileTypeEnum;
import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import com.qiniuyun.aibased3dmodelgen.model.vo.Model3DVO;
import com.qiniuyun.aibased3dmodelgen.service.AppService;
import com.qiniuyun.aibased3dmodelgen.service.Model3DService;
import com.qiniuyun.aibased3dmodelgen.service.Tripo3DService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/app")
@Slf4j
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private Tripo3DService tripo3DService;

    @Resource
    private Model3DService model3DService;

    @Resource
    private AiGeneratorFacade aiGeneratorFacade;

    @Resource(name = "tripoBlockingScheduler")
    private Scheduler tripoBlockingScheduler;


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
     * 使用SSE实时推送3D模型生成进度，并保存模型数据
     */
    @PostMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Model3DVO>> generateModelWithPromptProgress(
            @RequestBody @Valid ModelGenerateStreamRequest modelGenerateStreamRequest, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(modelGenerateStreamRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ModelGenerateRequest modelGenerateRequest = new ModelGenerateRequest();
        BeanUtils.copyProperties(modelGenerateStreamRequest, modelGenerateRequest);

        return tripo3DService.generateModelFromText(modelGenerateRequest)
                .publishOn(tripoBlockingScheduler)
                .flatMapMany(response -> {
                    String taskId = response.getTaskId();

                    // 创建轮询流，每5秒检查一次状态
                    return Flux.interval(Duration.ofSeconds(5))
                            .flatMap(tick -> tripo3DService.checkTaskStatus(taskId))
                            .publishOn(tripoBlockingScheduler)
                            .map(statusResponse -> {
                                // 保存或更新模型数据，传递用户的实际提示词
                                Model3D model3D = model3DService.saveOrUpdateModelFromText(statusResponse,
                                        modelGenerateStreamRequest.getPrompt(), request);
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
                    log.error("文生模型流式生成过程中发生错误: {}", e.getMessage(), e);
                    // 创建错误响应的VO对象
                    Model3DVO errorVO = new Model3DVO();
                    errorVO.setStatus("failed");
                    errorVO.setProgress(0);

                    return Flux.just(ServerSentEvent.<Model3DVO>builder()
                            .event("error")
                            .data(errorVO)
                            .build());
                });
    }


    /**
     * 使用SSE实时推送3D模型生成进度，并保存模型数据
     */
    @PostMapping(value = "/generate-stream-augmented", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Model3DVO>> generateModelWithAugmentedPromptProgress(
            @RequestBody @Valid ModelGenerateStreamRequest modelGenerateStreamRequest, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(modelGenerateStreamRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        // 使用增强的 prompt
        String prompt = modelGenerateStreamRequest.getPrompt();
        String augmentedPrompt = aiGeneratorFacade.generatePrompt(1L, prompt, ObjectGenTypeEnum.PBR);
        ThrowUtils.throwIf(augmentedPrompt == null, ErrorCode.SYSTEM_ERROR, "生成增强prompt失败");
        modelGenerateStreamRequest.setPrompt(augmentedPrompt);
        ModelGenerateRequest modelGenerateRequest = new ModelGenerateRequest();
        BeanUtils.copyProperties(modelGenerateStreamRequest, modelGenerateRequest);

        return tripo3DService.generateModelFromText(modelGenerateRequest)
                .publishOn(tripoBlockingScheduler)
                .flatMapMany(response -> {
                    String taskId = response.getTaskId();

                    // 创建轮询流，每5秒检查一次状态
                    return Flux.interval(Duration.ofSeconds(5))
                            .flatMap(tick -> tripo3DService.checkTaskStatus(taskId))
                            .publishOn(tripoBlockingScheduler)
                            .map(statusResponse -> {
                                // 保存或更新模型数据
                                Model3D model3D = model3DService.saveOrUpdateModelFromText(statusResponse, augmentedPrompt, request);
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
                    log.error("增强文生模型流式生成过程中发生错误: {}", e.getMessage(), e);
                    // 创建错误响应的VO对象
                    Model3DVO errorVO = new Model3DVO();
                    errorVO.setStatus("failed");
                    errorVO.setProgress(0);

                    return Flux.just(ServerSentEvent.<Model3DVO>builder()
                            .event("error")
                            .data(errorVO)
                            .build());
                });
    }


    /**
     * 使用SSE实时推送3D模型生成进度，并保存模型数据
     */
    @PostMapping(value = "/generate-stream-image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Model3DVO>> generateModelWithImageProgress(
            @RequestPart("file") MultipartFile picture, ImageGenerateStreamRequest imageGenerateStreamRequest,
            HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        // 校验图片
        appService.validPicture(picture);
        // 获取图片类型
        String pictureType = appService.getPictureType(picture);
        // 上传图片到云存储
        UploadFileTypeEnum imageType = UploadFileTypeEnum.USER_UPLOADED;

        ImageToModelRequest imageToModelRequest = new ImageToModelRequest();
        BeanUtils.copyProperties(imageGenerateStreamRequest, imageToModelRequest);

        // 将可能阻塞的图片上传放到自定义线程池中执行
        return Mono.fromCallable(() -> appService.uploadFile(picture, imageType))
                .subscribeOn(tripoBlockingScheduler)
                .doOnNext(uploadedPictureUrl -> log.info("开始图片转模型任务，图片URL: {}, 类型: {}", uploadedPictureUrl, pictureType))
                .flatMapMany(uploadedPictureUrl -> 
                    tripo3DService.generateModelFromImage(uploadedPictureUrl, pictureType, imageToModelRequest)
                        .publishOn(tripoBlockingScheduler)
                        .flatMapMany(response -> {
                            String taskId = response.getTaskId();
                            log.info("图片转模型任务已创建，任务ID: {}", taskId);
                            // 创建轮询流，每5秒检查一次状态
                            return Flux.interval(Duration.ofSeconds(5))
                                    .flatMap(tick -> tripo3DService.checkTaskStatus(taskId))
                                    .publishOn(tripoBlockingScheduler)
                                    .map(statusResponse -> {
                                        // 保存或更新模型数据，使用专门的图片转模型方法
                                        Model3D model3D = model3DService.saveOrUpdateModelFromImage(statusResponse, uploadedPictureUrl, request);
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
                )
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
    }
}