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

    
    // 未来可扩展的字段
    // private String quality;      // 模型质量：low, medium, high
    // private String style;        // 模型风格
    // private Boolean enablePbr;   // 是否启用PBR材质
    // private String userId;       // 用户ID（用于权限控制）
}