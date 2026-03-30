package com.example.li_ai_code_mother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 【用户】创建应用请求。
 */
@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /** 
     * 应用初始化的 prompt（必填）
     */
    private String initPrompt;

    /**
     * 序列化
     */
    private static final long serialVersionUID = 1L;
}

