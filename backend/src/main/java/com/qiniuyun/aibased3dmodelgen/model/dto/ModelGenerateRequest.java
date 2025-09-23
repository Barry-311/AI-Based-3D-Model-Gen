package com.qiniuyun.aibased3dmodelgen.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelGenerateRequest {

    private String type = "text_to_model"; // 根据文档，固定为 "text_to_model"
    private String prompt;

}
