package com.base.project.common;


/**
 * @author mrbeard
 */

public enum ResultCodeEnum {
    /**
     * 成功
     */
    SUCCESS(200,"success"),
    /**
     * 未登录
     */
    NOT_LOING(300,"unLogin"),
    /**
     * 失败
     */
    FAIL(400,"fail"),
    /**
     * 未认证（签名错误）
     */
    UNAUTHORIZED(401,"unAuthority"),
    /**
     * 接口不存在
     */
    NOT_FOUND(404,"unFind"),
    /**
     * 服务器内部错误
     */
    COMMON_SERVER_ERROR(500,"error");

    /**
     * 响应code
     */
    public Integer code;
    /**
     * 响应codeName
     */
    public String message;

    ResultCodeEnum(int code,String message){
        this.code = code;
        this.message = message;
    }

    public int getCode(){
        return code;
    }

    public String getMessage() {
        return message;
    }

}

