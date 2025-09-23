package com.qiniuyun.aibased3dmodelgen.service;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public interface ObjectDownloadService {

    /**
     * 下载模型
     * @param projectPath
     * @param downloadFileName
     * @param response
     * @return
     */
    void downloadObject(String projectPath, String downloadFileName, HttpServletResponse response);
}
