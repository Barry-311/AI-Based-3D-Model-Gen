package com.qiniuyun.aibased3dmodelgen.ai;

import com.qiniuyun.aibased3dmodelgen.exception.BusinessException;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import com.qiniuyun.aibased3dmodelgen.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * AI 生成服务工厂
 */
@Configuration
@Slf4j
public class AiGeneratorServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    /**
     * 创建 AI 代码生成器服务
     * @param appId
     * @param objectGenType
     * @return
     */
    private AiGeneratorService createAiGeneratorService(long appId, ObjectGenTypeEnum objectGenType) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        return switch (objectGenType) {
            // OBJ
            case OBJ -> {
                // 使用多例模式的 StreamingChatModel 解决并发问题
                StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(openAiStreamingChatModel)
                        .build();
            }
            default ->
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的模型生成类型: " + objectGenType.getValue());
        };
    }

    /**
     * 根据 appId 获取服务
     * @param appId
     * @param objectGenType
     * @return
     */
    public AiGeneratorService getAiGeneratorService(long appId, ObjectGenTypeEnum objectGenType) {
        return createAiGeneratorService(appId, objectGenType);
    }
}
