package com.qiniuyun.aibased3dmodelgen.ai;

import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiGeneratorService {

    /**
     * 生成 PBR prompt（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/gen-3d-obj-prompt.txt")
    Flux<String> generatePBRStream(String userMessage);


    /**
     * 生成 PBR prompt（非流式）
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/gen-3d-obj-prompt.txt")
    String generatePBR(String userMessage);
}
