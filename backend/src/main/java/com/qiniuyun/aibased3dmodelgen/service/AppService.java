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
     * @param multipartFile
     * @return
     */
    String uploadPicture(MultipartFile multipartFile);


    /**
     * 验证图片
     * @param inputSource
     */
    void validPicture(Object inputSource);

    /**
     * 获取图片类型
     * @param multipartFile
     * @return
     */
    String getPictureType(MultipartFile multipartFile);
}
