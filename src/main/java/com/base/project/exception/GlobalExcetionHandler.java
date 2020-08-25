package com.base.project.exception;

import com.base.project.common.Result;
import com.base.project.common.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 *
 * @author mrbeard
 * @date 2020/08/25
 */
@Slf4j
@ControllerAdvice
public class GlobalExcetionHandler {


    /**
     * 处理自定义异常
     *
     * @param e e
     * @return {@link Result}
     */
    @ExceptionHandler(CommonException.class)
    @ResponseBody
    public Result handleCustomException(CommonException e){
        log.error("error:",e);
        return new Result(ResultCodeEnum.COMMON_SERVER_ERROR);
    }

    /**
     * 处理异常
     *
     * @param e e
     * @return {@link Result}
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception e){
        log.error("error:",e);
        return new Result(ResultCodeEnum.COMMON_SERVER_ERROR);
    }

}