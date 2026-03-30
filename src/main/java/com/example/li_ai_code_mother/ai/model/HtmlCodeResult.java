package com.example.li_ai_code_mother.ai.model;


import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
import org.bouncycastle.crypto.signers.ISOTrailers;

/**
 * Html代码返回结果
 *
 *
 */


@Description("生成html代码文件的结果")
@Data
public class HtmlCodeResult {

    /**
     * HTML代码
     *
     */
    @Description("Html代码")
    private String htmlCode;


    /**
     * 多余的描述
     *
     */
    @Description("生成代码的描述")
    private String descritption;

}
