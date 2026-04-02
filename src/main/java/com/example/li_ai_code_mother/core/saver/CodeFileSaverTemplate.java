package com.example.li_ai_code_mother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.li_ai_code_mother.constant.AppConstant;
import com.example.li_ai_code_mother.exception.BusinessException;
import com.example.li_ai_code_mother.exception.ErrorCode;
import com.example.li_ai_code_mother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器 - 模板方法模式
 * @param <T>
 */
public abstract class CodeFileSaverTemplate<T> {
//    private static final String File_Save_Root_Dir = System.getProperty("user.dir") + AppConstant.CODE_OUTPUT_ROOT_DIR;
    private static final String File_Save_Root_Dir = AppConstant.CODE_OUTPUT_ROOT_DIR;


    /**
     * 模板方法，保存代码的标准流程
     *
     * @param result
     * @return
     */
    public final File saveCode(T result , Long appID){
        //1.验证输入
        validateInput(result);

        //2. 构建唯一目录
        String baseDirPath = buildUniqueDir(appID);

        //3. 保存文件（具体实现交给子类）
        saveFiles(result , baseDirPath);

        //4. 返回文件目录对象
        return new File(baseDirPath);
    }



    protected void validateInput(T result) {
        if(result == null)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"代码对象不能为空");
    }


    /**
     *构建文件的唯一路径（tmp/code_output/biztype_雪花id）
     * @param: Appid
     * @return 目录路径
     */
    protected String buildUniqueDir(Long appID){
        if(appID == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用ID不能为空");
        }
        String codeType = getCodeType().getValue();
        String uniquedirname = StrUtil.format("{}_{}", codeType , appID);
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
    public final void writeToFile(String dirpath , String filename , String content ){
        if (StrUtil.isNotBlank(content)){
            String filePath = dirpath + File.separator + filename;
            FileUtil.writeString(content , filePath , StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取代码生成类型
     *
     * @return 类型
     */
    protected abstract CodeGenTypeEnum getCodeType();


    /**
     * 保存文件具体交给子类
     *
     * @param result
     * @param baseDirPath
     */
    protected abstract void saveFiles(T result, String baseDirPath);
}
