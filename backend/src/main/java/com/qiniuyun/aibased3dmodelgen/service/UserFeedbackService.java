package com.qiniuyun.aibased3dmodelgen.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.qiniuyun.aibased3dmodelgen.model.dto.user.UserQueryRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.userFeedback.UserFeedbackQueryRequest;
import com.qiniuyun.aibased3dmodelgen.model.entity.UserFeedback;
import com.qiniuyun.aibased3dmodelgen.model.vo.UserFeedbackVO;

import java.util.List;

/**
 * 用户反馈表 服务层。
 *
 * @author <a href="https://github.com/RJLante">RJLante</a>
 */
public interface UserFeedbackService extends IService<UserFeedback> {

    /**
     * 获取脱敏后的 UserFeedback
     * @param userFeedback
     * @return
     */
    UserFeedbackVO getUserFeedbackVO(UserFeedback userFeedback);

    /**
     * 获取脱敏后的 UserFeedback 列表
     * @param userFeedbackList
     * @return
     */
    List<UserFeedbackVO> getUserFeedbackVOList(List<UserFeedback> userFeedbackList);

    /**
     * 获取用户查询条件
     * @param userFeedbackQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserFeedbackQueryRequest userFeedbackQueryRequest);
}
