package com.example.li_ai_code_mother.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息响应基类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor  //无参构造函数
public class StreamMessage {
    private String type;
}
