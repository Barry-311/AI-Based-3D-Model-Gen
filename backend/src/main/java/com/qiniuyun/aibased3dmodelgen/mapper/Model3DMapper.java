package com.qiniuyun.aibased3dmodelgen.mapper;

import com.mybatisflex.core.BaseMapper;
import com.qiniuyun.aibased3dmodelgen.model.entity.Model3D;
import org.apache.ibatis.annotations.Mapper;

/**
 * 3D模型数据访问层
 */
@Mapper
public interface Model3DMapper extends BaseMapper<Model3D> {
}
