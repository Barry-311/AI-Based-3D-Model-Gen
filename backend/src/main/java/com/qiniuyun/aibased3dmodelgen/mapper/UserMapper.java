package com.qiniuyun.aibased3dmodelgen.mapper;

import com.mybatisflex.core.BaseMapper;
import com.qiniuyun.aibased3dmodelgen.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 映射层。
 *
 * @author <a href="https://github.com/RJLante">RJLante</a>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
