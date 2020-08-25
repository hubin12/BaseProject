package com.base.project.common;


import lombok.Data;

/**
 * 统一返回结果
 *
 * @author mrbeard
 * @date 2020/08/25
 */
@Data
public class Result {
    private Integer code;
    private String message;
    private Object data;

    public Result() { }

    public Result(ResultCodeEnum resultCodeEnum) {
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }

}
