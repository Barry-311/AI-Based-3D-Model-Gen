package com.qiniuyun.aibased3dmodelgen.service.impl;

import com.qiniuyun.aibased3dmodelgen.exception.BusinessException;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.exception.ThrowUtils;
import com.qiniuyun.aibased3dmodelgen.service.ObjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
@Slf4j
public class ObjectDownloadServiceImpl implements ObjectDownloadService {

    @Override
    public void downloadObject(String objectDirPath, String objectName, HttpServletResponse response) {
        // 基础校验
        ThrowUtils.throwIf(StrUtil.isBlank(objectName), ErrorCode.PARAMS_ERROR, "文件名不能为空");
        String filePath = objectDirPath + File.separator + objectName;
        File objectFile = new File(filePath);
        ThrowUtils.throwIf(!objectFile.exists(), ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        ThrowUtils.throwIf(!objectFile.isFile(), ErrorCode.PARAMS_ERROR, "指定路径不是文件");

        log.info("开始下载文件: {}", filePath);

        // 设置 HTTP 响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition",
                String.format("attachment; filename=\"%s\"", objectName));
        response.setContentLength((int) objectFile.length());

        try (FileInputStream fis = new FileInputStream(objectFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            log.info("文件下载完成: {}", objectName);
        } catch (IOException e) {
            log.error("文件下载异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件下载失败");
        }
    }
}
