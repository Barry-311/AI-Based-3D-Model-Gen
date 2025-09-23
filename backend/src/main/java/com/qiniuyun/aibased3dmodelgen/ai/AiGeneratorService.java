package com.qiniuyun.aibased3dmodelgen.ai;

import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiGeneratorService {

    /**
     * 生成 OBJ prompt
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-3d-obj-prompt.txt")
    Flux<String> generateOBJStream(String userMessage);
}
