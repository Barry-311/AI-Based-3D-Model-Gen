package com.qiniuyun.aibased3dmodelgen.core;

import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiGeneratorFacadeTest {

    @Resource
    private AiGeneratorFacade aiGeneratorFacade;
    @Test
    void generateStream() {
        Flux<String> codeStream = aiGeneratorFacade.generatePromptStream(1L,"生成一艘小船，要大概5m长", ObjectGenTypeEnum.PBR);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        // 拼接字符串，得到完整内容
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }
}