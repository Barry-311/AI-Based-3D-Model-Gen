package com.qiniuyun.aibased3dmodelgen.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Model3DVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * Tripo3D 任务 ID
     */
    private String taskId;

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 状态
     */
    private String status;

    /**
     * 进度
     */
    private Integer progress;

    /**
     * PBR 模型 URL
     */
    private String pbrModelUrl;

    /**
     * 渲染图片 URL
     */
    private String renderedImageUrl;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
