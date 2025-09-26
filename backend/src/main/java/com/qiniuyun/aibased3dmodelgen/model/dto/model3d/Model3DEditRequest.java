package com.qiniuyun.aibased3dmodelgen.model.dto.model3d;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Model3DEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
