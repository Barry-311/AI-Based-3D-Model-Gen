package com.qiniuyun.aibased3dmodelgen.service;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public interface ObjectDownloadService {

    /**
     * 下载模型
     * @param projectPath
     * @param objectName
     * @param response
     */
    void downloadObject(String projectPath, String objectName, HttpServletResponse response);
}
