package com.qiniuyun.aibased3dmodelgen.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户反馈表 实体类。
 *
 * @author <a href="https://github.com/RJLante">RJLante</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_feedback")
public class UserFeedback implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 反馈类型：model_quality(模型质量), user_experience(用户体验), feature_request(功能建议), bug_report(问题反馈)
     */
    @Column("feedbackType")
    private String feedbackType;

    /**
     * 评分(1-5分)
     */
    @Column("rating")
    private Integer rating;

    /**
     * 反馈标题
     */
    @Column("title")
    private String title;

    /**
     * 反馈内容
     */
    @Column("content")
    private String content;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
