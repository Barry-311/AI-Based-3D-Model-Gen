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

}
