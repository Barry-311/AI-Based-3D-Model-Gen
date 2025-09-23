package com.qiniuyun.aibased3dmodelgen.service;

import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface AppService {

    Flux<String> augmentPrompt(Long appId, String message, ObjectGenTypeEnum objectGenTypeEnum);
}
