package com.example.li_ai_code_mother.controller;

import cn.hutool.core.util.StrUtil;
import com.example.li_ai_code_mother.annotation.AuthCheck;
import com.example.li_ai_code_mother.common.BaseResponse;
import com.example.li_ai_code_mother.common.DeleteRequest;
import com.example.li_ai_code_mother.common.ResultUtils;
import com.example.li_ai_code_mother.constant.UserConstant;
import com.example.li_ai_code_mother.exception.ErrorCode;
import com.example.li_ai_code_mother.exception.ThrowUtils;
import com.example.li_ai_code_mother.model.dto.app.AppAdminQueryRequest;
import com.example.li_ai_code_mother.model.dto.app.AppAdminUpdateRequest;
import com.example.li_ai_code_mother.model.entity.App;
import com.example.li_ai_code_mother.service.AppService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员 - 应用 控制层。
 */
@RestController
@RequestMapping("/app/admin")
public class AppAdminController {

    @Resource
    private AppService appService;

    /**
     * 【管理员】根据 id 删除任意应用
     */
//    @PostMapping("/delete")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Boolean> adminRemoveApp(@RequestBody DeleteRequest deleteRequest) {
//        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
//        boolean result = appService.adminRemoveApp(deleteRequest);
//        return ResultUtils.success(result);
//    }

    /**
     * 【管理员】根据 id 更新任意应用（支持更新应用名称、应用封面、优先级）
     */
//    @PostMapping("/update")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest request) {
//        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
//
//        // 字段级校验（具体校验逻辑在 service 内也会兜底）
//        if (request.getAppName() != null) {
//            ThrowUtils.throwIf(StrUtil.hasBlank(request.getAppName()), ErrorCode.PARAMS_ERROR, "appName不能为空");
//        }
//        if (request.getCover() != null) {
//            ThrowUtils.throwIf(StrUtil.hasBlank(request.getCover()), ErrorCode.PARAMS_ERROR, "cover不能为空");
//        }
//
//        boolean result = appService.adminUpdateApp(request);
//        return ResultUtils.success(result);
//    }

    /**
     * 【管理员】分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<App>> adminListAppsByPage(@RequestBody AppAdminQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getPageNum() <= 0 || request.getPageSize() <= 0, ErrorCode.PARAMS_ERROR);

        Page<App> page = appService.adminListAppsByPage(request);
        return ResultUtils.success(page);
    }

    /**
     * 【管理员】根据 id 查看应用详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<App> adminGetAppById(@RequestParam("id") Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.adminGetAppById(id);
        return ResultUtils.success(app);
    }
}

