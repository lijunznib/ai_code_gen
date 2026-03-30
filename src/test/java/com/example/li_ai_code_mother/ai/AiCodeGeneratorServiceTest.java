//package com.example.li_ai_code_mother.ai;
//
//import com.example.li_ai_code_mother.ai.model.HtmlCodeResult;
//import com.example.li_ai_code_mother.ai.model.MultiFilecodeResult;
//import jakarta.annotation.Resource;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class AiCodeGeneratorServiceTest {
//
//    @Resource
//    private AiCodeGeneratorService aiCodeGeneratorService;
//
//    @Test
//    void generateHtmlCode() {
//        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个20行的博客");
//        Assertions.assertNotNull(result);
//    }
//
//    @Test
//    void generateMultiFileCode() {
//        MultiFilecodeResult result = aiCodeGeneratorService.generateMultiFileCode("做个20行的留言板");
//        Assertions.assertNotNull(result);
//    }
//}