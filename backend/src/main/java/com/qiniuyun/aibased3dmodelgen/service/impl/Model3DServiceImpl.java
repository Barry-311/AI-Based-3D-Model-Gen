package com.qiniuyun.aibased3dmodelgen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.qiniuyun.aibased3dmodelgen.exception.BusinessException;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.exception.ThrowUtils;
import com.qiniuyun.aibased3dmodelgen.mapper.Model3DMapper;
import com.qiniuyun.aibased3dmodelgen.model.dto.TaskStatusResponse;
import com.qiniuyun.aibased3dmodelgen.model.dto.model3d.Model3DEditRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.model3d.Model3DQueryRequest;
import com.qiniuyun.aibased3dmodelgen.model.entity.Model3D;
import com.qiniuyun.aibased3dmodelgen.model.entity.User;
import com.qiniuyun.aibased3dmodelgen.model.vo.Model3DVO;
import com.qiniuyun.aibased3dmodelgen.service.Model3DService;
import com.qiniuyun.aibased3dmodelgen.service.Tripo3DService;
import com.qiniuyun.aibased3dmodelgen.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Model3DServiceImpl extends ServiceImpl<Model3DMapper, Model3D> implements Model3DService {

    @Resource
    private UserService userService;

    @Resource
    private Tripo3DService tripo3DService;

    @Override
    public Model3D saveOrUpdateModelFromText(TaskStatusResponse taskStatusResponse, String prompt, HttpServletRequest request) {
        return saveOrUpdateModelInternal(taskStatusResponse, null, prompt, request);
    }

    @Override
    public Model3D saveOrUpdateModelFromImage(TaskStatusResponse taskStatusResponse, String pictureUrl,
                                              HttpServletRequest request) {
        return saveOrUpdateModelInternal(taskStatusResponse, pictureUrl, "图片转模型", request);
    }

    private Model3D saveOrUpdateModelInternal(TaskStatusResponse taskStatusResponse, String pictureUrl,
                                              String prompt, HttpServletRequest request) {
        String taskId = taskStatusResponse.getData().getTaskId();
        Model3D existingModel = getByTaskId(taskId);
        User loginUser = userService.getLoginUser(request);
        Model3D model3D;

        if (existingModel != null) {
            model3D = existingModel;
        } else {
            model3D = new Model3D();
            model3D.setTaskId(taskId);
            model3D.setUserId(loginUser.getId());
            model3D.setName("model3D_" + taskId);
            model3D.setPrompt(prompt);
            model3D.setCreateTime(LocalDateTime.now());
            if (StringUtils.isNotBlank(pictureUrl)) {
                model3D.setPictureUrl(pictureUrl);
            }
        }

        model3D.setStatus(taskStatusResponse.getStatus());
        model3D.setProgress(taskStatusResponse.getProgress());
        model3D.setUpdateTime(LocalDateTime.now());

        // 如果任务完成，并且我们还没有处理过文件（通过检查URL字段是否为空来防止重复执行）
        if ("success".equals(taskStatusResponse.getStatus()) &&
                StringUtils.isBlank(model3D.getPbrModelUrl()) && // 关键：防止重复处理
                taskStatusResponse.getData() != null &&
                taskStatusResponse.getData().getOutput() != null) {

            try {
                log.info("任务 {} 已成功，开始下载和上传模型资源.", taskId);
                TaskStatusResponse.Output output = taskStatusResponse.getData().getOutput();

                // 获取临时的下载URL
                String tempRenderedImageUrl = output.getRenderedImage();
                String tempPbrModelUrl = output.getPbrModel();

                // 调用TripoService的组合方法，完成下载和上传
                Tripo3DService.AssetUrls finalUrls = tripo3DService.downloadAndUploadAssets(tempRenderedImageUrl, tempPbrModelUrl);

                // 将返回的最终COS URL设置到模型对象中
                model3D.setPbrModelUrl(finalUrls.getModelUrl());
                model3D.setRenderedImageUrl(finalUrls.getImageUrl());
                log.info("模型资源已成功上传到COS并准备更新数据库记录，任务ID: {}", taskId);

            } catch (Exception e) {
                // 如果在下载或上传过程中发生异常
                log.error("处理成功的任务资源时失败，任务ID: {}", taskId, e);
                // 更新状态，以便前端或运维知晓
                model3D.setStatus("processing_failed");
            }
        }

        // 统一在这里执行保存或更新操作
        saveOrUpdate(model3D);
        return model3D;
    }
    
    @Override
    public Model3D getByTaskId(String taskId) {
        // 使用字符串字段名而不是TableDef
        return getOne(QueryWrapper.create().eq("taskId", taskId));
    }


    @Override
    public Model3DVO getModel3DVO(Model3D model3D) {
        if (model3D == null) {
            return null;
        }
        Model3DVO model3DVO = new Model3DVO();
        BeanUtil.copyProperties(model3D, model3DVO);
        return model3DVO;
    }

    @Override
    public void editModel3D(Model3DEditRequest model3DEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Model3D model3D = new Model3D();
        BeanUtils.copyProperties(model3DEditRequest, model3D);
        // 注意将 list 转为 string
        model3D.setName(model3DEditRequest.getName());
        // 设置编辑时间
        model3D.setUpdateTime(LocalDateTime.now());
        // 判断是否存在
        long id = model3DEditRequest.getId();
        Model3D oldModel3D = this.getById(id);
        ThrowUtils.throwIf(oldModel3D == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = this.updateById(model3D);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public List<Model3DVO> getModel3DVOList(List<Model3D> model3dList) {
        if (CollUtil.isEmpty(model3dList)) {
            return new ArrayList<>();
        }
        return model3dList.stream().map(this::getModel3DVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(Model3DQueryRequest model3DQueryRequest) {
        if (model3DQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = model3DQueryRequest.getId();
        String taskId = model3DQueryRequest.getTaskId();
        String name = model3DQueryRequest.getName();
        String prompt = model3DQueryRequest.getPrompt();
        String status = model3DQueryRequest.getStatus();
        Long userId = model3DQueryRequest.getUserId();
        LocalDateTime createTime = model3DQueryRequest.getCreateTime();
        String sortField = model3DQueryRequest.getSortField();
        String sortOrder = model3DQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .eq("taskId", taskId)
                .like("name", name)
                .like("prompt", prompt)
                .eq("status", status)
                .eq("userId", userId)
                .ge("createTime", createTime)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }




}