package com.changgou.framework.exception;

import entity.Result;
import entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: Ye Jian Song
 * @Description: 统一异常处理器
 * @Date: Create in 16:34 2019/8/11
 */
@ControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return new Result(false, StatusCode.ERROR,e.getMessage());
    }

}
