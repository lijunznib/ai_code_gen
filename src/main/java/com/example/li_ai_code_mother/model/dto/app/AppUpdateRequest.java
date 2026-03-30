package com.example.li_ai_code_mother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 【用户】根据 id 修改应用名称请求。
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long id;

    /**
     * 应用名称（必填）
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}

