package com.qiniuyun.aibased3dmodelgen.model.dto.model3d;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
public class Model3DUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * Tripo3D 任务 ID
     */
    private String taskId;

    /**
     * 模型名称
     */
    private String name;

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
     * 用户上传的图片 URL
     */
    private String pictureUrl;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否公开
     */
    private Integer isPublic;

    private static final long serialVersionUID = 1L;
}
