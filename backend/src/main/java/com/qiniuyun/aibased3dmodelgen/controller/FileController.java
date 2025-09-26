package com.qiniuyun.aibased3dmodelgen.controller;

import com.qiniuyun.aibased3dmodelgen.common.BaseResponse;
import com.qiniuyun.aibased3dmodelgen.common.ResultUtils;
import com.qiniuyun.aibased3dmodelgen.constant.ObjectConstant;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.exception.ThrowUtils;
import com.qiniuyun.aibased3dmodelgen.service.AppService;
import com.qiniuyun.aibased3dmodelgen.service.ObjectDownloadService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private AppService appService;

    @Resource
    private ObjectDownloadService objectDownloadService;

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传文件为空");
        appService.validPicture(multipartFile);
        String result = appService.uploadPicture(multipartFile);
        return ResultUtils.success(result);
    }

    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    public void downloadObject(@PathVariable Long appId,
                               HttpServletResponse response) {
        // 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

        // 构建模型目录路径（生成目录）
        String sourceDirName = "Object_" + appId;
        String sourceDirPath = ObjectConstant.OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 检查目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "模型不存在，请先生成模型");
        String downloadFileName = appId + ".pbr";
        // 调用通用下载服务
        objectDownloadService.downloadObject(sourceDirPath, downloadFileName, response);
    }

}
