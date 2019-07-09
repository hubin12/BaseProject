package com.mrbeard.baseproject.result;

/**
 * @author mrbeard
 */
public class ResultGenerator {

    /**
     * 成功
     * @return
     */
    public static <T> Result<T> getSuccessResult(T data) {
        return new Result().setCode(ResultCodeEnum.SUCCESS).setMessage(ResultCodeEnum.SUCCESS.getCodeName()).setData(data);
    }

    /**
     * 失败
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> getErrorResult(T data) {
        return new Result().setCode(ResultCodeEnum.INTERNAL_SERVER_ERROR).setMessage(ResultCodeEnum.INTERNAL_SERVER_ERROR.getCodeName()).setData(data);
    }

    /**
     * 未登录
     * @return
     */
    public static Result getNotLoginResult(){
        return new Result().setCode(ResultCodeEnum.NOT_LOING).setMessage(ResultCodeEnum.NOT_LOING.getCodeName()).setData(ResultCodeEnum.NOT_LOING.getCodeName());
    }
}


