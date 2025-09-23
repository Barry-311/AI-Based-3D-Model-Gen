package com.qiniuyun.aibased3dmodelgen.service;

import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@Service
public interface AppService {

    /**
     * 模型生成
     * @param appId
     * @param message
     * @param objectGenTypeEnum
     * @return
     */
    Flux<String> augmentPrompt(Long appId, String message, ObjectGenTypeEnum objectGenTypeEnum);

    /**
     * 图片上传
     */
    boolean uploadPicture(MultipartFile multipartFile);
}
