package com.qiniuyun.aibased3dmodelgen.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelGenerateResponse {
    
    private int code;
    private ResponseData data;
    private String message;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseData {
        @JsonProperty("task_id")
        private String taskId;
    }
    
    // 为了向后兼容，保留原有的getTaskId方法
    public String getTaskId() {
        return data != null ? data.getTaskId() : null;
    }
}
