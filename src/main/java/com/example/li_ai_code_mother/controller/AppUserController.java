package com.example.li_ai_code_mother.controller;

import cn.hutool.core.util.StrUtil;
import com.example.li_ai_code_mother.common.DeleteRequest;
import com.example.li_ai_code_mother.common.BaseResponse;
import com.example.li_ai_code_mother.common.ResultUtils;
import com.example.li_ai_code_mother.exception.ErrorCode;
import com.example.li_ai_code_mother.exception.ThrowUtils;
import com.example.li_ai_code_mother.model.dto.app.*;
import com.example.li_ai_code_mother.model.entity.App;
import com.example.li_ai_code_mother.service.AppService;
import com.example.li_ai_code_mother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 用户 - 应用 控制层。
 */
@RestController
@RequestMapping("/app/user")
public class AppUserController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 【用户】创建应用（须填写 initPrompt）
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.hasBlank(appAddRequest.getInitPrompt()), ErrorCode.PARAMS_ERROR, "initPrompt不能为空");

        Long userId = userService.getLoginUser(request).getId();
        long appId = appService.userAddApp(appAddRequest, userId);
        return ResultUtils.success(appId);
    }

    /**
     * 【用户】根据 id 修改自己的应用（目前只支持修改应用名称）
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateAppName(@RequestBody AppUpdateRequest updateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(updateRequest == null || updateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.hasBlank(updateRequest.getAppName()), ErrorCode.PARAMS_ERROR, "appName不能为空");

        Long userId = userService.getLoginUser(request).getId();
        boolean result = appService.userUpdateAppName(updateRequest, userId);
        return ResultUtils.success(result);
    }

    /**
     * 【用户】根据 id 删除自己的应用
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> removeApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Long userId = userService.getLoginUser(request).getId();
        boolean result = appService.userRemoveApp(deleteRequest, userId);
        return ResultUtils.success(result);
    }

    /**
     * 【用户】根据 id 查看应用详情
     */
    @GetMapping("/get")
    public BaseResponse<App> getAppById(@RequestParam("id") Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Long userId = userService.getLoginUser(request).getId();
        App app = appService.userGetAppById(id, userId);
        return ResultUtils.success(app);
    }

    /**
     * 【用户】分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<App>> listMyAppsByPage(@RequestBody AppQueryRequest queryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(queryRequest.getPageSize() > 20, ErrorCode.PARAMS_ERROR, "每页最多20个");

        Long userId = userService.getLoginUser(request).getId();
        Page<App> page = appService.userListAppsByPage(queryRequest, userId);
        return ResultUtils.success(page);
    }

    /**
     * 【用户】分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     */
    @PostMapping("/list/page/good")
    public BaseResponse<Page<App>> listGoodAppsByPage(@RequestBody AppQueryRequest queryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(queryRequest.getPageSize() > 20, ErrorCode.PARAMS_ERROR, "每页最多20个");

        // 登录校验：精选应用列表也要求用户已登录（由 getLoginUser 保障）
        userService.getLoginUser(request);
        Page<App> page = appService.userListGoodAppsByPage(queryRequest);
        return ResultUtils.success(page);
    }
}

