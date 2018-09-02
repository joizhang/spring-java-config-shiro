package com.joizhang.imooc.controller.support;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author https://github.com/SeanDragon/protools/blob/master/http/src/main/java/pro/tools/http/pojo/HttpReceive.java
 */
@Getter
@Setter
public class HttpReceive {
    /**
     * 错误信息
     */
    private String errMsg;
    /**
     * 异常本体
     */
    private Throwable throwable;
    /**
     * 状态码
     */
    private Integer statusCode;
    /**
     * 状态文本
     */
    private String statusText;
    /**
     * 响应内容
     */
    private String responseBody;
    /**
     * 响应的header列表
     */
    private Map<String, String> responseHeader;

    /**
     * 是否有异常
     */
    private Boolean haveError = false;
    /**
     * 是否执行完成
     */
    private Boolean isDone = false;

}
