package com.qiniuyun.aibased3dmodelgen.controller;

import com.qiniuyun.aibased3dmodelgen.constant.ObjectConstant;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.exception.ThrowUtils;
import com.qiniuyun.aibased3dmodelgen.service.ObjectDownloadService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/app")
public class AppController {
    @Resource
    private ObjectDownloadService objectDownloadService;

    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId,
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
        String downloadFileName = appId + ".obj";
        // 调用通用下载服务
        objectDownloadService.downloadObject(sourceDirPath, downloadFileName, response);
    }
}