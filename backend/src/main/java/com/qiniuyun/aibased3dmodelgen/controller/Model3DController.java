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
import com.qiniuyun.aibased3dmodelgen.model.dto.model3d.Model3DEditRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.model3d.Model3DQueryRequest;
import com.qiniuyun.aibased3dmodelgen.model.dto.model3d.Model3DUpdateRequest;
import com.qiniuyun.aibased3dmodelgen.model.entity.Model3D;
import com.qiniuyun.aibased3dmodelgen.model.entity.User;
import com.qiniuyun.aibased3dmodelgen.model.vo.Model3DVO;
import com.qiniuyun.aibased3dmodelgen.service.Model3DService;
import com.qiniuyun.aibased3dmodelgen.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/model")
public class Model3DController {

    @Resource
    private Model3DService model3DService;

    @Resource
    private UserService userService;

    /**
     * 根据 id 获取模型（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Model3D> getModel3DById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Model3D model3D = model3DService.getById(id);
        ThrowUtils.throwIf(model3D == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(model3D);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<Model3DVO> getModel3DVOById(long id) {
        BaseResponse<Model3D> response = getModel3DById(id);
        Model3D model3D = response.getData();
        return ResultUtils.success(model3DService.getModel3DVO(model3D));
    }

    /**
     * 删除模型
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteModel3D(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = model3DService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新模型（管理员）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateModel3D(@RequestBody Model3DUpdateRequest model3DUpdateRequest) {
        if (model3DUpdateRequest == null || model3DUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Model3D model3D = new Model3D();
        BeanUtil.copyProperties(model3DUpdateRequest, model3D);
        boolean result = model3DService.updateById(model3D);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新模型（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editModel3D(@RequestBody Model3DEditRequest model3DEditRequest, HttpServletRequest request) {
        if (model3DEditRequest == null || model3DEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        model3DService.editModel3D(model3DEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param model3DQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Model3DVO>> listModelVOByPage(@RequestBody Model3DQueryRequest model3DQueryRequest) {
        ThrowUtils.throwIf(model3DQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = model3DQueryRequest.getPageNum();
        long pageSize = model3DQueryRequest.getPageSize();
        Page<Model3D> model3DPage = model3DService.page(Page.of(pageNum, pageSize),
                model3DService.getQueryWrapper(model3DQueryRequest));
        // 数据脱敏
        Page<Model3DVO> model3DVOPage = new Page<>(pageNum, pageSize, model3DPage.getTotalRow());
        List<Model3DVO> mdoelVOList = model3DService.getModel3DVOList(model3DPage.getRecords());
        model3DVOPage.setRecords(mdoelVOList);
        return ResultUtils.success(model3DVOPage);
    }
}
