package com.example.li_ai_code_mother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("生成多个代码文件的结果")
public class MultiFilecodeResult {
    /**
     * HTML代码
     *
     */
    @Description("HTML代码")
    private String htmlCode;

    /**
     * css代码
     *
     */
    @Description("css代码")
    private String cssCode;

    /**
     * js代码
     *
     */
    @Description("js代码")
    private String jsCode;


    /**
     * 多余的描述
     *
     */
    @Description("生成代码的描述")
    private String descritption;

}
