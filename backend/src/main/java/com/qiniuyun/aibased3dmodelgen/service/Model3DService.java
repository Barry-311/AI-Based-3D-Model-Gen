package com.qiniuyun.aibased3dmodelgen.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.qiniuyun.aibased3dmodelgen.model.dto.model3d.Model3DQueryRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.user.UserQueryRequest;
import com.qiniuyun.aibased3dmodelgen.model.entity.Model3D;
import com.qiniuyun.aibased3dmodelgen.model.dto.TaskStatusResponse;
import com.qiniuyun.aibased3dmodelgen.model.entity.User;
import com.qiniuyun.aibased3dmodelgen.model.vo.Model3DVO;
import com.qiniuyun.aibased3dmodelgen.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface Model3DService extends IService<Model3D> {
    
    /**
     * 保存或更新模型数据
     * @param taskStatusResponse 任务状态响应
     * @return 模型实体
     */
    /**
     * 保存或更新模型（文本转模型）
     */
    Model3D saveOrUpdateModel(TaskStatusResponse taskStatusResponse, HttpServletRequest request);
    
    /**
     * 保存或更新模型（图片转模型）
     */
    Model3D saveOrUpdateModelFromImage(TaskStatusResponse taskStatusResponse, String pictureUrl, HttpServletRequest request);
    
    /**
     * 根据taskId查询模型
     */
    Model3D getByTaskId(String taskId);

    /**
     * 根据模型数据生成VO
     * @param model3D
     * @return
     */
    Model3DVO getModel3DVO(Model3D model3D);

    /**
     * 获取模型列表
     * @param model3dList
     * @return
     */
    List<Model3DVO> getModel3DVOList(List<Model3D> model3dList);

    /**
     * 获取模型查询条件
     * @param model3DQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(Model3DQueryRequest model3DQueryRequest);
}