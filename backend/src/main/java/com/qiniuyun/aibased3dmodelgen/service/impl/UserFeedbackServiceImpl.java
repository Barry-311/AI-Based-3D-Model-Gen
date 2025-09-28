package com.qiniuyun.aibased3dmodelgen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.qiniuyun.aibased3dmodelgen.exception.BusinessException;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.model.dto.userFeedback.UserFeedbackQueryRequest;
import com.qiniuyun.aibased3dmodelgen.model.entity.UserFeedback;
import com.qiniuyun.aibased3dmodelgen.mapper.UserFeedbackMapper;
import com.qiniuyun.aibased3dmodelgen.model.vo.UserFeedbackVO;
import com.qiniuyun.aibased3dmodelgen.model.vo.UserVO;
import com.qiniuyun.aibased3dmodelgen.service.UserFeedbackService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户反馈表 服务层实现。
 *
 * @author <a href="https://github.com/RJLante">RJLante</a>
 */
@Service
public class UserFeedbackServiceImpl extends ServiceImpl<UserFeedbackMapper, UserFeedback>  implements UserFeedbackService{

    @Override
    public UserFeedbackVO getUserFeedbackVO(UserFeedback userFeedback) {
        if (userFeedback == null) {
            return null;
        }
        UserFeedbackVO userFeedbackVO = new UserFeedbackVO();
        BeanUtil.copyProperties(userFeedback, userFeedbackVO);
        return userFeedbackVO;
    }

    @Override
    public List<UserFeedbackVO> getUserFeedbackVOList(List<UserFeedback> userFeedbackList) {
        if (CollUtil.isEmpty(userFeedbackList)) {
            return new ArrayList<>();
        }
        return userFeedbackList.stream().map(this::getUserFeedbackVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserFeedbackQueryRequest userFeedbackQueryRequest) {
        if (userFeedbackQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userFeedbackQueryRequest.getId();
        Long userId = userFeedbackQueryRequest.getUserId();
        String feedbackType = userFeedbackQueryRequest.getFeedbackType();
        Integer rating = userFeedbackQueryRequest.getRating();
        String title = userFeedbackQueryRequest.getTitle();
        String content = userFeedbackQueryRequest.getContent();
        String sortField = userFeedbackQueryRequest.getSortField();
        String sortOrder = userFeedbackQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .eq("userId", userId)
                .eq("feedbackType", feedbackType)
                .eq("rating", rating)
                .like("title", title)
                .like("content", content)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }
}
