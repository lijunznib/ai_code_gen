package com.example.li_ai_code_mother.service;

import com.example.li_ai_code_mother.model.entity.User;
import com.example.li_ai_code_mother.model.vo.AppVO;
import com.mybatisflex.core.service.IService;
import com.example.li_ai_code_mother.model.entity.App;
import com.example.li_ai_code_mother.common.DeleteRequest;
import com.example.li_ai_code_mother.model.dto.app.AppAdminQueryRequest;
import com.example.li_ai_code_mother.model.dto.app.AppAdminUpdateRequest;
import com.example.li_ai_code_mother.model.dto.app.AppAddRequest;
import com.example.li_ai_code_mother.model.dto.app.AppUpdateRequest;
import com.example.li_ai_code_mother.model.dto.app.AppQueryRequest;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/lijunznib">李J</a>
 */
public interface AppService extends IService<App> {


    /**
     * 创建应用
     * @param appAddRequest
     * @param loginUser
     * @return
     */
    Long createApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 获取用户封装类
     * @param app
     * @return
     */
    AppVO getAppVO(App app);


    /**
     * 获取应用封装列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构造应用查询条件
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


    /**
     * 聊天生成代码
     * @param appID
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appID , String message , User loginUser);

    /**
     * 网站应用部署
     * @param appID
     * @param loginUser
     * @return
     */
    String deployApp(Long appID , User loginUser);

    void generateAppScreenshotAsync(Long appId, String appUrl);

    long userAddApp(AppAddRequest request, Long userId);

    boolean userUpdateAppName(AppUpdateRequest request, Long userId);

    boolean userRemoveApp(DeleteRequest deleteRequest, Long userId);

    App userGetAppById(Long id, Long userId);

    Page<App> userListAppsByPage(AppQueryRequest request, Long userId);

    Page<App> userListGoodAppsByPage(AppQueryRequest request);

    boolean adminRemoveApp(DeleteRequest deleteRequest);

    boolean adminUpdateApp(AppAdminUpdateRequest request);

    Page<App> adminListAppsByPage(AppAdminQueryRequest request);

    App adminGetAppById(Long id);

    QueryWrapper getUserAppsQueryWrapper(AppQueryRequest request, Long userId);

    QueryWrapper getGoodAppsQueryWrapper(AppQueryRequest request);

    QueryWrapper getAdminAppsQueryWrapper(AppAdminQueryRequest request);

}
