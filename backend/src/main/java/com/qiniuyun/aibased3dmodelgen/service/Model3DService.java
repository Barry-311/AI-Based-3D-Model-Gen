package com.qiniuyun.aibased3dmodelgen.service;

import com.mybatisflex.core.service.IService;
import com.qiniuyun.aibased3dmodelgen.model.entity.Model3D;
import com.qiniuyun.aibased3dmodelgen.model.dto.TaskStatusResponse;
import com.qiniuyun.aibased3dmodelgen.model.vo.Model3DVO;

public interface Model3DService extends IService<Model3D> {
    
    /**
     * 保存或更新模型数据
     * @param taskStatusResponse 任务状态响应
     * @return 模型实体
     */
    /**
     * 保存或更新模型（文本转模型）
     */
    Model3D saveOrUpdateModel(TaskStatusResponse taskStatusResponse);
    
    /**
     * 保存或更新模型（图片转模型）
     */
    Model3D saveOrUpdateModelFromImage(TaskStatusResponse taskStatusResponse, String pictureUrl);
    
    /**
     * 根据taskId查询模型
     */
    Model3D getByTaskId(String taskId);
    
    /**
     * 下载并保存模型文件到本地
     */
    void downloadAndSaveModel(Model3D model3D);

    /**
     * 根据模型数据生成VO
     * @param model3D
     * @return
     */
    Model3DVO getModel3DVO(Model3D model3D);
}