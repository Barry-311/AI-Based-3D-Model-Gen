package com.qiniuyun.aibased3dmodelgen.service.impl;

import cn.hutool.core.util.StrUtil;
import com.qiniuyun.aibased3dmodelgen.core.AiGeneratorFacade;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.exception.ThrowUtils;
import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import com.qiniuyun.aibased3dmodelgen.service.AppService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AppServiceImpl implements AppService {

    @Resource
    private AiGeneratorFacade aiGeneratorFacade;

    @Override
    public Flux<String> augmentPrompt(Long appId, String message, ObjectGenTypeEnum objectGenTypeEnum) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        return aiGeneratorFacade.generateStream(appId, message, objectGenTypeEnum);
    }
}
