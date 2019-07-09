package com.mrbeard.baseproject.common;

/**
 * @author mrbeard
 * @date 2018-11-07 17:59
 * @description
 */
public interface Constant {
    String PARAM_LOSS = "参数错误";
    String ERROR_BROWERTOKEN = "用户识别码异常，请尝试刷新重试";
    String USER_NOT_EXIST = "用户名不存在，请联系管理员";
    String ERROR_PASSWORD = "请输入正确的密码";
    String USER_DISABLED = "该用户已经被禁用";
    String RANDOMCODE_TIMEOUT = "验证码已失效";
    String RANDOMCODE_ERROR = "请输入正确的验证码";
    String NOT_LOGIN_ERROR = "用户未登录";
    String FILE_UNEXIT = "文件不存在";
    String ERROR_IN_SUBMISSION_MODE = "提交方式出错";
    String DATA_ERROR = "数据错误";
    String UNSAMPLE_PASSWORD = "两次密码不一致";
    /**
     * 用户状态：启用
     */
    int STATE_OPEN = 1;

    /**
     * 用户状态：禁用
     */
    String STATE_CLOSE = "0";

    /**
     *
     * 用户token的有效期 单位：秒
     */
    int USER_TOKEN_INVALIDTIME = 60*60;
    /**
     * 系统识别码token有效期 单位：秒
     */
    int BORWER_TOKEN_INVALIDTIME = 60*10;

    /**
     * 操作类型：新增
     */
    int OPERATION_INSERT = 1;
    /**
     * 操作类型：更新
     */
    int OPERATION_UPDATE = 2;
    /**
     * 操作类型：删除
     */
    int OPERATION_DELETE = 3;

}

