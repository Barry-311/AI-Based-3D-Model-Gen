package com.qiniuyun.aibased3dmodelgen.service.impl;

import cn.hutool.core.util.StrUtil;
import com.qiniuyun.aibased3dmodelgen.core.AiGeneratorFacade;
import com.qiniuyun.aibased3dmodelgen.exception.BusinessException;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.exception.ThrowUtils;
import com.qiniuyun.aibased3dmodelgen.manager.CosManager;
import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import com.qiniuyun.aibased3dmodelgen.service.AppService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;

@Service
@Slf4j
public class AppServiceImpl implements AppService {

    @Resource
    private AiGeneratorFacade aiGeneratorFacade;

    @Resource
    private CosManager cosManager;

    @Override
    public Flux<String> augmentPrompt(Long appId, String message, ObjectGenTypeEnum objectGenTypeEnum) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        return aiGeneratorFacade.generatePromptStream(appId, message, objectGenTypeEnum);
    }

    @Override
    public boolean uploadPicture(MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/picture/%s", filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            // 返回可访问的地址
            String uploadFileUrl = cosManager.uploadFile(filepath, file);
            // todo 调用 API
            return true;
        } catch (Exception e) {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean isDelete = file.delete();
                if (!isDelete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }


}
