package com.example.li_ai_code_mother.model.dto.app;

import com.example.li_ai_code_mother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 【管理员】应用分页查询请求。
 *
 * 注意：不包含时间字段，避免与需求中的“除时间外字段查询”冲突。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppAdminQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private String appName;

    private String cover;

    private String initPrompt;

    private String codeGenType;

    private String deployKey;

    private Integer priority;

    private Long userId;

    /**
     * 是否删除（逻辑删除字段）
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}

