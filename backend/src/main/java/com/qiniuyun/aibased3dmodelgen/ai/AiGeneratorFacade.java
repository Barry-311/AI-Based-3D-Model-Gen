package com.qiniuyun.aibased3dmodelgen.ai;

import com.qiniuyun.aibased3dmodelgen.ai.AiGeneratorService;
import com.qiniuyun.aibased3dmodelgen.ai.AiGeneratorServiceFactory;
import com.qiniuyun.aibased3dmodelgen.exception.BusinessException;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.model.dto.ModelGenerateResponse;
import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import com.qiniuyun.aibased3dmodelgen.service.Tripo3DService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class AiGeneratorFacade {

    @Resource
    private AiGeneratorServiceFactory aiGeneratorServiceFactory;
    @Resource
    private Tripo3DService tripo3DService;


    public Mono<ModelGenerateResponse> generateModelFromAugmentedText(Long appId, String message, ObjectGenTypeEnum objectGenTypeEnum) {
        String augmentedPrompt = generatePrompt(appId, message, objectGenTypeEnum);
        return tripo3DService.generateModelFromText(augmentedPrompt);
    }

    public String generatePrompt(Long appId, String message, ObjectGenTypeEnum objectGenTypeEnum) {
        if (objectGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取对应的 AI 服务实例
        AiGeneratorService aiGeneratorService = aiGeneratorServiceFactory.getAiGeneratorService(appId, objectGenTypeEnum);
        return switch (objectGenTypeEnum) {
            case PBR -> aiGeneratorService.generatePBR(message);
            default -> {
                String errorMessage = "不支持的生成类型：" + objectGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    public Flux<String> generatePromptStream(Long appId, String message, ObjectGenTypeEnum objectGenTypeEnum) {
        if (objectGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取对应的 AI 服务实例
        AiGeneratorService aiGeneratorService = aiGeneratorServiceFactory.getAiGeneratorService(appId, objectGenTypeEnum);
        return switch (objectGenTypeEnum) {
            case PBR -> aiGeneratorService.generatePBRStream(message);
            default -> {
                String errorMessage = "不支持的生成类型：" + objectGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


}
