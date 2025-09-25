package com.qiniuyun.aibased3dmodelgen.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 3D模型实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("model_3d")
public class Model3D implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    @Column("taskId")
    private String taskId;

    @Column("prompt")
    private String prompt;

    @Column("status")
    private String status;

    @Column("progress")
    private Integer progress;

    @Column("modelUrl")
    private String modelUrl;

    @Column("baseModelUrl")
    private String baseModelUrl;

    @Column("pbrModelUrl")
    private String pbrModelUrl;

    @Column("renderedImageUrl")
    private String renderedImageUrl;

    @Column("pictureUrl")
    private String pictureUrl;

    @Column("localModelPath")
    private String localModelPath;

    @Column("fileSize")
    private Long fileSize;

    @Column("createTime")
    private LocalDateTime createTime;

    @Column("updateTime")
    private LocalDateTime updateTime;

    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;
}
