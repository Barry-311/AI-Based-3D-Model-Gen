package com.qiniuyun.aibased3dmodelgen.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.qiniuyun.aibased3dmodelgen.ai.AiGeneratorFacade;
import com.qiniuyun.aibased3dmodelgen.exception.BusinessException;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.exception.ThrowUtils;
import com.qiniuyun.aibased3dmodelgen.manager.CosManager;
import com.qiniuyun.aibased3dmodelgen.model.enums.UploadFileTypeEnum;
import com.qiniuyun.aibased3dmodelgen.model.enums.ObjectGenTypeEnum;
import com.qiniuyun.aibased3dmodelgen.service.AppService;
import com.qiniuyun.aibased3dmodelgen.service.Model3DService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class AppServiceImpl implements AppService {

    @Resource
    private AiGeneratorFacade aiGeneratorFacade;

    @Resource
    private CosManager cosManager;

    @Resource
    private Model3DService model3DService;

    @Override
    public Flux<String> augmentPrompt(Long appId, String message, ObjectGenTypeEnum objectGenTypeEnum) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        return aiGeneratorFacade.generatePromptStream(appId, message, objectGenTypeEnum);
    }

    // 处理用户直接上传的场景
    @Override
    public String uploadFile(MultipartFile multipartFile, UploadFileTypeEnum uploadFileTypeEnum) {
        String filename = multipartFile.getOriginalFilename();
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile("upload_", "_" + filename);
            multipartFile.transferTo(file);
            // 调用我们新的、基于File的上传方法
            return uploadFile(file, filename, uploadFileTypeEnum);
        } catch (Exception e) {
            log.error("MultipartFile upload error, filename = {}", filename, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 确保临时文件被删除
            if (file != null) {
                deleteTempFile(file);
            }
        }
    }

    /**
     * 重载方法，用于上传本地File对象到COS
     * @param file 要上传的本地文件
     * @param originalFilename 希望在COS中保存的文件名
     * @param uploadFileTypeEnum 文件类型，决定COS中的存储路径
     * @return 上传到COS后的可访问URL
     */
    @Override
    public String uploadFile(File file, String originalFilename, UploadFileTypeEnum uploadFileTypeEnum) {
        String filepath;
        switch (uploadFileTypeEnum) {
            case USER_UPLOADED -> filepath = String.format("/picture/%s", originalFilename);
            case RENDERED_IMAGE -> filepath = String.format("/rendered_image/%s", originalFilename);
            case PBR_MODEL -> filepath = String.format("/pbr_model/%s", originalFilename);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
        try {
            // 直接使用已有的File对象上传到COS
            log.info("开始上传文件到COS，路径: {}", filepath);
            String cosUrl = cosManager.uploadFile(filepath, file);
            log.info("文件成功上传到COS，URL: {}", cosUrl);
            return cosUrl;
        } catch (Exception e) {
            log.error("COS upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传到对象存储失败");
        }
    }

    /**
     * 辅助方法，用于安全删除文件
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            boolean isDelete = file.delete();
            if (!isDelete) {
                log.error("临时文件删除失败, filepath = {}", file.getAbsolutePath());
            }
        }
    }


    @Override
    public void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > ONE_M * 2, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
        // 校验文件类型
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件后缀列表（或者集合）
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("png", "jpg", "jpeg", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式不支持");
    }


    @Override
    public String getPictureType(MultipartFile multipartFile) {
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        return switch (fileSuffix) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
