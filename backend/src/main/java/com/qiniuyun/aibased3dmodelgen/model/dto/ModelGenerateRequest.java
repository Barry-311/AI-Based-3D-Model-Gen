package com.qiniuyun.aibased3dmodelgen.model.dto;

import com.qiniuyun.aibased3dmodelgen.model.enums.ModelGenTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型生成请求参数（用于请求 API）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelGenerateRequest {

    private String type = ModelGenTypeEnum.TEXT.getValue(); // 根据文档，固定为 "text_to_model"
    private String prompt;

    /**
     * 模型种子
     */
    private int model_seed;

    /**
     * 纹理种子
     */
    private int texture_seed;

    /**
     * 纹理质量：standard, detailed
     */
    private String texture_quality;

    /**
     * 模型质量：standard, detailed
     */
    private String geometry_quality;

    /**
     * 模型风格
     */
    private String style;

    /**
     * 启用纹理贴图功能
     */
    private boolean texture;

    /**
     * 模型最大面数 默认 10000
     */
    private int face_limit;

    /**
     * 自动调整模型大小
     */
    private boolean auto_size;

    /**
     * 模型压缩 空值，geometry
     */
    private String compress;
}
