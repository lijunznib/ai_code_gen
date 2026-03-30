package com.example.li_ai_code_mother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.example.li_ai_code_mother.model.entity.User;
import com.example.li_ai_code_mother.model.vo.AppVO;
import com.example.li_ai_code_mother.model.vo.UserVO;
import com.example.li_ai_code_mother.service.UserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.li_ai_code_mother.model.entity.App;
import com.example.li_ai_code_mother.mapper.AppMapper;
import com.example.li_ai_code_mother.service.AppService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import cn.hutool.core.util.StrUtil;
import com.example.li_ai_code_mother.common.DeleteRequest;
import com.example.li_ai_code_mother.constant.AppConstant;
import com.example.li_ai_code_mother.exception.BusinessException;
import com.example.li_ai_code_mother.exception.ErrorCode;
import com.example.li_ai_code_mother.model.dto.app.AppAdminQueryRequest;
import com.example.li_ai_code_mother.model.dto.app.AppAdminUpdateRequest;
import com.example.li_ai_code_mother.model.dto.app.AppAddRequest;
import com.example.li_ai_code_mother.model.dto.app.AppUpdateRequest;
import com.example.li_ai_code_mother.model.dto.app.AppQueryRequest;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/lijunznib">李J</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;


    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }


    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }



    @Override
    public long userAddApp(AppAddRequest request, Long userId) {
        if (request == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String initPrompt = request.getInitPrompt();
        if (StrUtil.hasBlank(initPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt不能为空");
        }

        App app = new App();
        app.setAppName(request.getAppName());
        app.setCover(request.getCover());
        app.setInitPrompt(initPrompt);
        app.setPriority(AppConstant.DEFAULT_APP_PRIORITY);
        app.setUserId(userId);
        app.setIsDelete(0);

        boolean saved = this.save(app);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "应用创建失败");
        }
        return app.getId();
    }

    @Override
    public boolean userUpdateAppName(AppUpdateRequest request, Long userId) {
        if (request == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = request.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String appName = request.getAppName();
        if (StrUtil.hasBlank(appName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "appName不能为空");
        }

        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!Objects.equals(app.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }

        App update = new App();
        update.setId(id);
        update.setAppName(appName);

        boolean result = this.updateById(update);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "应用更新失败");
        }
        return true;
    }

    @Override
    public boolean userRemoveApp(DeleteRequest deleteRequest, Long userId) {
        if (deleteRequest == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!Objects.equals(app.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }

        boolean result = this.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "应用删除失败");
        }
        return true;
    }

    @Override
    public App userGetAppById(Long id, Long userId) {
        if (id == null || id <= 0 || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!Objects.equals(app.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }
        return app;
    }

    @Override
    public Page<App> userListAppsByPage(AppQueryRequest request, Long userId) {
        if (request == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        if (pageNum <= 0 || pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "每页最多20个");
        }

        return this.page(Page.of(pageNum, pageSize), getUserAppsQueryWrapper(request, userId));
    }

    @Override
    public Page<App> userListGoodAppsByPage(AppQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        if (pageNum <= 0 || pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "每页最多20个");
        }

        return this.page(Page.of(pageNum, pageSize), getGoodAppsQueryWrapper(request));
    }

    @Override
    public boolean adminRemoveApp(DeleteRequest deleteRequest) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        boolean result = this.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "应用删除失败");
        }
        return true;
    }

    @Override
    public boolean adminUpdateApp(AppAdminUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = request.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        App origin = this.getById(id);
        if (origin == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String appName = request.getAppName();
        String cover = request.getCover();
        Integer priority = request.getPriority();

        boolean hasUpdate = false;
        if (appName != null) {
            if (StrUtil.hasBlank(appName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "appName不能为空");
            }
            hasUpdate = true;
        }
        if (cover != null) {
            if (StrUtil.hasBlank(cover)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "cover不能为空");
            }
            hasUpdate = true;
        }
        if (priority != null) {
            hasUpdate = true;
        }
        if (!hasUpdate) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未提供需要更新的字段");
        }

        App update = new App();
        update.setId(id);
        if (appName != null) {
            update.setAppName(appName);
        }
        if (cover != null) {
            update.setCover(cover);
        }
        if (priority != null) {
            update.setPriority(priority);
        }

        boolean result = this.updateById(update);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "应用更新失败");
        }
        return true;
    }

    @Override
    public Page<App> adminListAppsByPage(AppAdminQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        if (pageNum <= 0 || pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        return this.page(Page.of(pageNum, pageSize), getAdminAppsQueryWrapper(request));
    }

    @Override
    public App adminGetAppById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return app;
    }

    @Override
    public QueryWrapper getUserAppsQueryWrapper(AppQueryRequest request, Long userId) {
        QueryWrapper wrapper = QueryWrapper.create().eq("userId", userId);
        if (request == null) {
            return wrapper;
        }

        String appName = request.getAppName();
        if (StrUtil.isNotBlank(appName)) {
            wrapper.like("appName", appName);
        }

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            wrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        return wrapper;
    }

    @Override
    public QueryWrapper getGoodAppsQueryWrapper(AppQueryRequest request) {
        QueryWrapper wrapper = QueryWrapper.create().eq("priority", AppConstant.GOOD_APP_PRIORITY);
        if (request == null) {
            return wrapper;
        }

        String appName = request.getAppName();
        if (StrUtil.isNotBlank(appName)) {
            wrapper.like("appName", appName);
        }

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            wrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        return wrapper;
    }

    @Override
    public QueryWrapper getAdminAppsQueryWrapper(AppAdminQueryRequest request) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (request == null) {
            return wrapper;
        }

        Long id = request.getId();
        if (id != null && id > 0) {
            wrapper.eq("id", id);
        }
        String appName = request.getAppName();
        if (StrUtil.isNotBlank(appName)) {
            wrapper.like("appName", appName);
        }
        String cover = request.getCover();
        if (StrUtil.isNotBlank(cover)) {
            wrapper.like("cover", cover);
        }
        String initPrompt = request.getInitPrompt();
        if (StrUtil.isNotBlank(initPrompt)) {
            wrapper.like("initPrompt", initPrompt);
        }
        String codeGenType = request.getCodeGenType();
        if (StrUtil.isNotBlank(codeGenType)) {
            wrapper.like("codeGenType", codeGenType);
        }
        String deployKey = request.getDeployKey();
        if (StrUtil.isNotBlank(deployKey)) {
            wrapper.like("deployKey", deployKey);
        }
        Integer priority = request.getPriority();
        if (priority != null) {
            wrapper.eq("priority", priority);
        }
        Long userId = request.getUserId();
        if (userId != null && userId > 0) {
            wrapper.eq("userId", userId);
        }
        Integer isDelete = request.getIsDelete();
        if (isDelete != null) {
            wrapper.eq("isDelete", isDelete);
        }

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            wrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        return wrapper;
    }
}
