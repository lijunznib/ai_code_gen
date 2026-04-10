package com.example.li_ai_code_mother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.li_ai_code_mother.ai.AiCodeGenTypeRoutingService;
import com.example.li_ai_code_mother.ai.AiCodeGenTypeRoutingServiceFactory;
import com.example.li_ai_code_mother.core.AiCodeGeneratorFacade;
import com.example.li_ai_code_mother.core.builder.VueProjectBuilder;
import com.example.li_ai_code_mother.core.handler.StreamHandlerExecutor;
import com.example.li_ai_code_mother.exception.ThrowUtils;
import com.example.li_ai_code_mother.model.entity.User;
import com.example.li_ai_code_mother.model.enums.ChatHistoryMessageTypeEnum;
import com.example.li_ai_code_mother.model.enums.CodeGenTypeEnum;
import com.example.li_ai_code_mother.model.vo.AppVO;
import com.example.li_ai_code_mother.model.vo.UserVO;
import com.example.li_ai_code_mother.service.ChatHistoryService;
import com.example.li_ai_code_mother.service.ScreenshotService;
import com.example.li_ai_code_mother.service.UserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.li_ai_code_mother.model.entity.App;
import com.example.li_ai_code_mother.mapper.AppMapper;
import com.example.li_ai_code_mother.service.AppService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.View;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/lijunznib">李J</a>
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;
    @Resource
    private ScreenshotService screenshotService;
    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Autowired
    private ChatHistoryService chatHistoryService;
    @Autowired
    private View error;
    @Autowired
    private StreamHandlerExecutor streamHandlerExecutor;
    @Autowired
    private VueProjectBuilder vueProjectBuilder;
    @Autowired
    private RestClient.Builder builder;
    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用 AI 智能选择代码生成类型(多例模式)
        AiCodeGenTypeRoutingService routingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum selectedCodeGenType = routingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }



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
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 通过校验后，添加用户消息到对话历史
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 6. 调用 AI 生成代码（流式）
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodestream(message, codeGenTypeEnum, appId);
        // 7. 收集 AI 响应内容并在完成后记录到对话历史(根据对应的流 )
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);

    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败：" + e.getMessage());
        }
        // 9. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 10. 得到返回可访问的 URL
        String appDeployUrl =  String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 11. 异步生成截图并且更新应用封面
        generateAppScreenshotAsync(appId,appDeployUrl);
        return appDeployUrl;
    }


    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }



    /**
     * 删除应用时,关联删除对话历史
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
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
