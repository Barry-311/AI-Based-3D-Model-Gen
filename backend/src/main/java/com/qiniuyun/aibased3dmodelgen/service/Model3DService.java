package com.qiniuyun.aibased3dmodelgen.service;

import com.mybatisflex.core.service.IService;
import com.qiniuyun.aibased3dmodelgen.model.entity.Model3D;
import com.qiniuyun.aibased3dmodelgen.model.dto.TaskStatusResponse;

public interface Model3DService extends IService<Model3D> {
    
    /**
     * 保存或更新模型数据
     */
    Model3D saveOrUpdateModel(TaskStatusResponse taskStatusResponse, String prompt);
    
    /**
     * 根据taskId查询模型
     */
    Model3D getByTaskId(String taskId);
    
    /**
     * 下载并保存模型文件到本地
     */
    void downloadAndSaveModel(Model3D model3D);
}