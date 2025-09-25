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
    
    // 模型版本，可选参数
    private String model_version = "v2.5-20250123"; // 默认版本
    
    // 图片文件信息
    private FileInfo file;
    
    // 添加缺失的参数
    private Boolean texture = true; // 是否生成纹理
    private Boolean pbr = true; // 是否生成PBR材质
    private String geometry_quality = "standard"; // 几何质量：standard, high

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