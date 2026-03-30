package com.example.li_ai_code_mother.core.parser;

import com.example.li_ai_code_mother.ai.model.HtmlCodeResult;
import com.example.li_ai_code_mother.exception.BusinessException;
import com.example.li_ai_code_mother.exception.ErrorCode;
import com.example.li_ai_code_mother.model.enums.CodeGenTypeEnum;

import java.util.concurrent.locks.Condition;

/**
 * 代码解析执行器
 */
public class CodeParserExecutor {
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();


    /**
     * 执行代码解析
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum){
        return switch(codeGenTypeEnum){
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型");
        };
    }
}
