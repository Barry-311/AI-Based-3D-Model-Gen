package com.qiniuyun.aibased3dmodelgen.service;

import com.qiniuyun.aibased3dmodelgen.model.enums.UploadFileTypeEnum;
import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;

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
     * 文件上传
     * @param multipartFile
     * @return
     */
    String uploadFile(MultipartFile multipartFile, UploadFileTypeEnum uploadFileTypeEnum);


    String uploadFile(File file, String originalFilename, UploadFileTypeEnum uploadFileTypeEnum);

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
