package com.qiniuyun.aibased3dmodelgen.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * 模型生成流请求（用于接口）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelGenerateStreamRequest {
    
    @NotBlank(message = "提示词不能为空")
    @Size(max = 1000, message = "提示词长度不能超过1000字符")
    private String prompt;

    /**
     * 模型种子
     */
    private int model_seed = 0;

    /**
     * 纹理种子
     */
    private int texture_seed = 0;

    /**
     * 纹理质量：standard, detailed
     */
    private String texture_quality = "standard";

    /**
     * 模型质量：standard, detailed
     */
    private String geometry_quality = "standard";

    /**
     * 模型风格 person:person2cartoon, animal:venom, object:clay, object:steampunk, object:christmas, object:barbie, gold, ancient_bronze
     */
    private String style;

    /**
     * 启用纹理贴图功能
     */
    private boolean texture = false;

    /**
     * 模型最大面数 默认 10000
     */
    private int face_limit = 10000;

    /**
     * 自动调整模型大小
     */
    private boolean auto_size = false;

    /**
     * 模型压缩 空值，geometry
     */
    private String compress = "";

}