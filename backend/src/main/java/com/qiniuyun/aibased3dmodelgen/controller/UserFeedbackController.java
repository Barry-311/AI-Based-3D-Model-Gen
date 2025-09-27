package com.qiniuyun.aibased3dmodelgen.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.qiniuyun.aibased3dmodelgen.annotation.AuthCheck;
import com.qiniuyun.aibased3dmodelgen.common.BaseResponse;
import com.qiniuyun.aibased3dmodelgen.common.DeleteRequest;
import com.qiniuyun.aibased3dmodelgen.common.ResultUtils;
import com.qiniuyun.aibased3dmodelgen.constant.UserConstant;
import com.qiniuyun.aibased3dmodelgen.exception.BusinessException;
import com.qiniuyun.aibased3dmodelgen.exception.ErrorCode;
import com.qiniuyun.aibased3dmodelgen.exception.ThrowUtils;
import com.qiniuyun.aibased3dmodelgen.model.dto.userFeedback.UserFeedbackAddRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.userFeedback.UserFeedbackEditRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.userFeedback.UserFeedbackQueryRequest;
import com.qiniuyun.aibased3dmodelgen.model.entity.User;
import com.qiniuyun.aibased3dmodelgen.model.entity.UserFeedback;
import com.qiniuyun.aibased3dmodelgen.model.vo.UserFeedbackVO;
import com.qiniuyun.aibased3dmodelgen.service.UserFeedbackService;
import com.qiniuyun.aibased3dmodelgen.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户反馈控制器
 *
 * @author <a href="https://github.com/RJLante">RJLante</a>
 */
@RestController
@RequestMapping("/feedback")
public class UserFeedbackController {

    @Resource
    private UserFeedbackService userFeedbackService;

    @Resource
    private UserService userService;

    /**
     * 创建用户反馈
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserFeedback(@RequestBody UserFeedbackAddRequest userFeedbackAddRequest, 
                                              HttpServletRequest request) {
        ThrowUtils.throwIf(userFeedbackAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        
        // 参数校验
        String feedbackType = userFeedbackAddRequest.getFeedbackType();
        String content = userFeedbackAddRequest.getContent();
        ThrowUtils.throwIf(feedbackType == null || feedbackType.trim().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "反馈类型不能为空");
        ThrowUtils.throwIf(content == null || content.trim().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "反馈内容不能为空");
        
        // 校验反馈类型
        if (!isValidFeedbackType(feedbackType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的反馈类型");
        }
        
        // 校验评分范围
        Integer rating = userFeedbackAddRequest.getRating();
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须在1-5分之间");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 创建反馈实体
        UserFeedback userFeedback = new UserFeedback();
        BeanUtil.copyProperties(userFeedbackAddRequest, userFeedback);
        userFeedback.setUserId(loginUser.getId());
        userFeedback.setCreateTime(LocalDateTime.now());
        userFeedback.setUpdateTime(LocalDateTime.now());
        
        // 保存到数据库
        boolean result = userFeedbackService.save(userFeedback);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建反馈失败");

        return ResultUtils.success(userFeedback.getId());
    }

    /**
     * 根据 id 获取反馈（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserFeedback> getUserFeedbackById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "ID参数错误");
        UserFeedback userFeedback = userFeedbackService.getById(id);
        ThrowUtils.throwIf(userFeedback == null, ErrorCode.NOT_FOUND_ERROR, "反馈不存在");
        return ResultUtils.success(userFeedback);
    }

    /**
     * 根据 id 获取反馈包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserFeedbackVO> getUserFeedbackVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "ID参数错误");
        UserFeedback userFeedback = userFeedbackService.getById(id);
        ThrowUtils.throwIf(userFeedback == null, ErrorCode.NOT_FOUND_ERROR, "反馈不存在");
        
        // 权限校验：只有管理员或反馈创建者可以查看
        User loginUser = userService.getLoginUser(request);
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()) && 
            !userFeedback.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该反馈");
        }
        
        return ResultUtils.success(userFeedbackService.getUserFeedbackVO(userFeedback));
    }

    /**
     * 编辑用户反馈（用户只能编辑自己的反馈）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserFeedback(@RequestBody UserFeedbackEditRequest userFeedbackEditRequest, 
                                                  HttpServletRequest request) {
        ThrowUtils.throwIf(userFeedbackEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        
        // 参数校验
        String feedbackType = userFeedbackEditRequest.getFeedbackType();
        String content = userFeedbackEditRequest.getContent();
        ThrowUtils.throwIf(feedbackType == null || feedbackType.trim().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "反馈类型不能为空");
        ThrowUtils.throwIf(content == null || content.trim().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "反馈内容不能为空");
        
        // 校验反馈类型
        if (!isValidFeedbackType(feedbackType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的反馈类型");
        }
        
        // 校验评分范围
        Integer rating = userFeedbackEditRequest.getRating();
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须在1-5分之间");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 查找原反馈记录
        UserFeedback existingFeedback = userFeedbackService.getById(userFeedbackEditRequest.getId());
        ThrowUtils.throwIf(existingFeedback == null, ErrorCode.NOT_FOUND_ERROR, "反馈不存在");
        
        // 权限校验：只有反馈创建者可以编辑
        if (!existingFeedback.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限编辑该反馈");
        }
        
        // 更新反馈信息
        UserFeedback userFeedback = new UserFeedback();
        BeanUtil.copyProperties(userFeedbackEditRequest, userFeedback);
        userFeedback.setId(existingFeedback.getId());
        userFeedback.setUserId(existingFeedback.getUserId());
        userFeedback.setCreateTime(existingFeedback.getCreateTime());
        userFeedback.setUpdateTime(LocalDateTime.now());
        
        boolean result = userFeedbackService.updateById(userFeedback);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新反馈失败");

        return ResultUtils.success(true);
    }

    /**
     * 删除反馈（仅管理员）
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserFeedback(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, 
                ErrorCode.PARAMS_ERROR, "删除参数错误");
        
        boolean result = userFeedbackService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 分页获取反馈列表（管理员查看所有，用户查看自己的）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserFeedbackVO>> listUserFeedbackVOByPage(
            @RequestBody UserFeedbackQueryRequest userFeedbackQueryRequest, 
            HttpServletRequest request) {
        ThrowUtils.throwIf(userFeedbackQueryRequest == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        
        User loginUser = userService.getLoginUser(request);
        
        // 如果不是管理员，只能查看自己的反馈
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            userFeedbackQueryRequest.setUserId(loginUser.getId());
        }
        
        long pageNum = userFeedbackQueryRequest.getPageNum();
        long pageSize = userFeedbackQueryRequest.getPageSize();
        
        Page<UserFeedback> userFeedbackPage = userFeedbackService.page(
                Page.of(pageNum, pageSize),
                userFeedbackService.getQueryWrapper(userFeedbackQueryRequest)
        );
        
        // 数据脱敏
        Page<UserFeedbackVO> userFeedbackVOPage = new Page<>(pageNum, pageSize, userFeedbackPage.getTotalRow());
        List<UserFeedbackVO> userFeedbackVOList = userFeedbackService.getUserFeedbackVOList(userFeedbackPage.getRecords());
        userFeedbackVOPage.setRecords(userFeedbackVOList);
        
        return ResultUtils.success(userFeedbackVOPage);
    }

    /**
     * 获取当前用户的反馈列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserFeedbackVO>> listMyUserFeedbackVOByPage(
            @RequestBody UserFeedbackQueryRequest userFeedbackQueryRequest, 
            HttpServletRequest request) {
        ThrowUtils.throwIf(userFeedbackQueryRequest == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        
        User loginUser = userService.getLoginUser(request);
        
        // 强制设置为当前用户ID
        userFeedbackQueryRequest.setUserId(loginUser.getId());
        
        long pageNum = userFeedbackQueryRequest.getPageNum();
        long pageSize = userFeedbackQueryRequest.getPageSize();
        
        Page<UserFeedback> userFeedbackPage = userFeedbackService.page(
                Page.of(pageNum, pageSize),
                userFeedbackService.getQueryWrapper(userFeedbackQueryRequest)
        );
        
        // 数据脱敏
        Page<UserFeedbackVO> userFeedbackVOPage = new Page<>(pageNum, pageSize, userFeedbackPage.getTotalRow());
        List<UserFeedbackVO> userFeedbackVOList = userFeedbackService.getUserFeedbackVOList(userFeedbackPage.getRecords());
        userFeedbackVOPage.setRecords(userFeedbackVOList);
        
        return ResultUtils.success(userFeedbackVOPage);
    }

    /**
     * 校验反馈类型是否有效
     */
    private boolean isValidFeedbackType(String feedbackType) {
        return "model_quality".equals(feedbackType) || 
               "user_experience".equals(feedbackType) || 
               "feature_request".equals(feedbackType) || 
               "bug_report".equals(feedbackType);
    }
}
