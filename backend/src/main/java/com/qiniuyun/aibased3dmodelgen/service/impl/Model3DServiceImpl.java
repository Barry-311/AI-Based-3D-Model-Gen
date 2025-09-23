package com.qiniuyun.aibased3dmodelgen.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.qiniuyun.aibased3dmodelgen.mapper.Model3DMapper;
import com.qiniuyun.aibased3dmodelgen.model.dto.TaskStatusResponse;
import com.qiniuyun.aibased3dmodelgen.model.entity.Model3D;
import com.qiniuyun.aibased3dmodelgen.service.Model3DService;
import com.qiniuyun.aibased3dmodelgen.service.Tripo3DService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;

@Service
@Slf4j
public class Model3DServiceImpl extends ServiceImpl<Model3DMapper, Model3D> implements Model3DService {
    
    @Autowired
    private Tripo3DService tripo3DService;
    
    @Override
    public Model3D saveOrUpdateModel(TaskStatusResponse taskStatusResponse, String prompt) {
        String taskId = taskStatusResponse.getData().getTaskId();
        
        // 查找现有记录
        Model3D existingModel = getByTaskId(taskId);
        
        Model3D model3D;
        if (existingModel != null) {
            model3D = existingModel;
        } else {
            model3D = new Model3D();
            model3D.setTaskId(taskId);
            model3D.setPrompt(prompt);
            model3D.setCreateTime(LocalDateTime.now());
        }
        
        // 更新状态信息
        model3D.setStatus(taskStatusResponse.getStatus());
        model3D.setProgress(taskStatusResponse.getProgress());
        model3D.setUpdateTime(LocalDateTime.now());
        
        // 如果任务完成，保存URL信息
        if ("success".equals(taskStatusResponse.getStatus()) && 
            taskStatusResponse.getData().getOutput() != null) {
            TaskStatusResponse.Output output = taskStatusResponse.getData().getOutput();
            model3D.setModelUrl(output.getModel());
            model3D.setBaseModelUrl(output.getBaseModel());
            model3D.setPbrModelUrl(output.getPbrModel());
            model3D.setRenderedImageUrl(output.getRenderedImage());
        }
        
        // 保存或更新
        saveOrUpdate(model3D);
        
        return model3D;
    }
    
    @Override
    public Model3D getByTaskId(String taskId) {
        // 使用字符串字段名而不是TableDef
        return getOne(QueryWrapper.create().eq("taskId", taskId));
    }
    
    @Override
    public void downloadAndSaveModel(Model3D model3D) {
        String modelUrl = model3D.getPbrModelUrl(); // 优先使用PBR模型
        if (modelUrl == null || modelUrl.isEmpty()) {
            modelUrl = model3D.getModelUrl(); // 备选普通模型
        }
        
        if (modelUrl == null || modelUrl.isEmpty()) {
            log.warn("模型URL为空，无法下载: {}", model3D.getTaskId());
            return;
        }
        
        try {
            log.info("开始下载模型文件: {}", modelUrl);
            
            // 下载模型文件
            byte[] modelData = tripo3DService.downloadModel(modelUrl).block();
            
            if (modelData != null && modelData.length > 0) {
                // 创建保存目录
                File saveDir = new File("tmp/models");
                if (!saveDir.exists()) {
                    saveDir.mkdirs();
                }
                
                // 保存文件
                String fileName = model3D.getTaskId() + "_model.glb";
                File modelFile = new File(saveDir, fileName);
                
                try (FileOutputStream fos = new FileOutputStream(modelFile)) {
                    fos.write(modelData);
                    
                    // 更新数据库记录
                    model3D.setLocalModelPath(modelFile.getAbsolutePath());
                    model3D.setFileSize((long) modelData.length);
                    model3D.setUpdateTime(LocalDateTime.now());
                    updateById(model3D);
                    
                    log.info("模型文件下载并保存成功: {}, 大小: {} KB", 
                            modelFile.getAbsolutePath(), modelData.length / 1024);
                }
            }
        } catch (Exception e) {
            log.error("下载模型文件失败: {}", model3D.getTaskId(), e);
        }
    }
}