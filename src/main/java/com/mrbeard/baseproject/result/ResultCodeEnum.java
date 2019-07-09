package com.mrbeard.baseproject.result;


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
    INTERNAL_SERVER_ERROR(500,"error");

    /**
     * 响应code
     */
    public int code;
    /**
     * 响应codeName
     */
    public String codeName;

    ResultCodeEnum(int code,String codeName){
        this.code = code;
        this.codeName = codeName;
    }

    public int getCode(){
        return code;
    }

    public String getCodeName() {
        return codeName;
    }

}

