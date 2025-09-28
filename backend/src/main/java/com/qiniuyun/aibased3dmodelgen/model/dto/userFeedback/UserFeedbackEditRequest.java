package com.qiniuyun.aibased3dmodelgen.model.dto.userFeedback;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserFeedbackEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 反馈类型：model_quality(模型质量), user_experience(用户体验), feature_request(功能建议), bug_report(问题反馈)
     */
    private String feedbackType;

    /**
     * 评分(1-5分)
     */
    private Integer rating;

    /**
     * 反馈标题
     */
    private String title;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    private static final long serialVersionUID = 1L;
}
