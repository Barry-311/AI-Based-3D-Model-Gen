package com.qiniuyun.aibased3dmodelgen.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qiniuyun.aibased3dmodelgen.model.enums.ModelGenTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片转模型请求参数（用于请求 API）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageToModelRequest {

    private String type = ModelGenTypeEnum.IMAGE.getValue(); // 根据文档，固定为 "image_to_model"

    /**
     * 模型版本
     */
    private String model_version = "v2.5-20250123"; // 默认版本

    /**
     * 启用纹理贴图功能
     */
    private boolean texture;

    /**
     * 纹理对齐方式 original_image, geometry
     */
    private String texture_alignment;

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

    /**
     * 图片文件信息
     */
    private FileInfo file;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL) // 只序列化非null字段
    public static class FileInfo {
        private String type; // 文件类型，如 "image/jpeg"
        private String file_token; // 上传后获得的文件token（与url互斥）
        private String url; // 直接使用URL（与file_token互斥）
    }
}