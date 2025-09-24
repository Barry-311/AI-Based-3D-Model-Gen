package com.qiniuyun.aibased3dmodelgen.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskStatusResponse {
    
    private int code;
    private TaskData data;
    private String message;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskData {
        @JsonProperty("task_id")
        private String taskId;
        private String type;
        private String status;
        private int progress;
        private Object input;
        private Output output;
        @JsonProperty("create_time")
        private long createTime;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Output {
        private String model;
        @JsonProperty("base_model")
        private String baseModel;
        @JsonProperty("pbr_model")
        private String pbrModel;
        @JsonProperty("rendered_image")
        private String renderedImage;
    }
    
    // 为了向后兼容，保留原有的方法
    public String getStatus() {
        return data != null ? data.getStatus() : null;
    }
    
    public int getProgress() {
        return data != null ? data.getProgress() : 0;
    }
    
    public Output getOutput() {
        return data != null ? data.getOutput() : null;
    }
}
