package com.qiniuyun.aibased3dmodelgen.model.dto.model3d;

import com.qiniuyun.aibased3dmodelgen.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class Model3DQueryRequest extends PageRequest implements Serializable {

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
     * 用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否公开
     */
    private Integer isPublic;

    private static final long serialVersionUID = 1L;
}
