package com.example.li_ai_code_mother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.li_ai_code_mother.ai.model.HtmlCodeResult;
import com.example.li_ai_code_mother.ai.model.MultiFilecodeResult;
import com.example.li_ai_code_mother.model.enums.CodeGenTypeEnum;
import okio.Utf8;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static jodd.util.PropertiesUtil.writeToFile;

/**
 * 文件保存器
 */
@Deprecated

public class CodeFileSaver {
    //文件保存的根目录
    private static final String File_Save_Root_Dir = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存 单文件网页代码 HtmlCodeResult
     */
    public static File saveHtmlCodeResult(HtmlCodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        return new File(baseDirPath);
    }

    /**
     * 保存 多文件网页代码 MultiFileCodeResult
     */
    public static File saveMultiFileCodeResult(MultiFilecodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
        return new File(baseDirPath);
    }


    /**
     *构建文件的唯一路径（tmp/code_output/biztype_雪花id）
     * @param biztype 代码生成类型
     * @return
     */
    private static String buildUniqueDir(String biztype){
        String uniquedirname = StrUtil.format("{}_{}",biztype , IdUtil.getSnowflakeNextIdStr());
        String dirpath = File_Save_Root_Dir + File.separator + uniquedirname;
        FileUtil.mkdir(dirpath);
        return dirpath;
    }



    /**
     * 保存单个文件
     * @param dirpath
     * @param filename
     * @param content
     */
    private static void writeToFile(String dirpath , String filename , String content ){
        String filePath = dirpath + File.separator + filename;
        FileUtil.writeString(content , filePath , StandardCharsets.UTF_8);
    }
}





