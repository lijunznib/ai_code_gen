package com.example.li_ai_code_mother;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.example.li_ai_code_mother.mapper")
public class LiAiCodeMotherApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiAiCodeMotherApplication.class, args);
    }
}



