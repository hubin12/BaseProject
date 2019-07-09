package com.mrbeard.baseproject.exception;

import com.mrbeard.baseproject.result.Result;
import com.mrbeard.baseproject.result.ResultGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

/**
 * @author mrbeard
 */
@ControllerAdvice
public class GlobalExcetionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExcetionHandler.class);

    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public Result handleCustemException(BaseException e){
        logger.warn("warn:",e);
        return ResultGenerator.getErrorResult(e.getMessage());
    }

    @ExceptionHandler(BaseRuntimeException.class)
    @ResponseBody
    public Result handleCustemException(BaseRuntimeException e){
        logger.warn("warn:",e);
        return ResultGenerator.getErrorResult(e.getMessage());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public Result handleConstraintViolationException(ConstraintViolationException e){
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder stringBuilder = new StringBuilder();
        for (ConstraintViolation<?> item : violations) {
            stringBuilder.append(item.getMessage()).append(",");
        }
        return ResultGenerator.getErrorResult(stringBuilder.toString());
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public Result handleBindException(BindException e){
        List<ObjectError> list = e.getAllErrors();
        String message = "";
        for (ObjectError error : list){
            message = error.getDefaultMessage();
            break;
        }
        logger.warn("warn:",e);
        return ResultGenerator.getErrorResult(message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public Result handleMaxUploadSizeExceededException(Exception e){
        return ResultGenerator.getErrorResult("图片太大，上传失败");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleExcpetion(Exception e){
        logger.error("error:",e);
        return ResultGenerator.getErrorResult(e.getMessage());
    }
}