package com.mrbeard.baseproject.aspect.model;

import java.util.Date;

/**
 * @Author mrbeard
 * @Date 2018/12/4 10:16
 * 交互日志实体
 **/
public class InteractiveLog {
    /**
     * 日志主键
     */
    private Integer logId;
    /**
     * 交互请求发起人
     */
    private String requester;
    /**
     * 姓名
     */
    private String requesterName;
    /**
     * 时间
     */
    private Date requestTime;
    /**
     * 请求IP:Port
     */
    private String requestFrom;
    /**
     * 请求参数
     */
    private String requestParameter;
    /**
     * 交互响应的 Controller 类型
     */
    private String responseClass;
    /**
     * 交互响应的方法
     */
    private String responseMethod;
    /**
     * 请求返回信息
     */
    private String responseMes;
    /**
     * 服务异常记录
     */
    private String exceptionMes;
    /**
     * 本次请求响应花费的时间
     */
    private Long interactiveSpend;

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestFrom() {
        return requestFrom;
    }

    public void setRequestFrom(String requestFrom) {
        this.requestFrom = requestFrom;
    }

    public String getRequestParameter() {
        return requestParameter;
    }

    public void setRequestParameter(String requestParameter) {
        this.requestParameter = requestParameter;
    }

    public String getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(String responseClass) {
        this.responseClass = responseClass;
    }

    public String getResponseMethod() {
        return responseMethod;
    }

    public void setResponseMethod(String responseMethod) {
        this.responseMethod = responseMethod;
    }

    public String getResponseMes() {
        return responseMes;
    }

    public void setResponseMes(String responseMes) {
        this.responseMes = responseMes;
    }

    public String getExceptionMes() {
        return exceptionMes;
    }

    public void setExceptionMes(String exceptionMes) {
        this.exceptionMes = exceptionMes;
    }

    public Long getInteractiveSpend() {
        return interactiveSpend;
    }

    public void setInteractiveSpend(Long interactiveSpend) {
        this.interactiveSpend = interactiveSpend;
    }

    @Override
    public String toString(){
        return "requester="+requester+" | requesterName="+requesterName+" | requestTime="+requestTime+
                " | requestFrom="+requestFrom+" | requestParameter="+requestParameter+" | responseClass="+responseClass+
                " | responseMethod="+responseMethod+" | responseMes="+responseMes+" | exceptionMes="+exceptionMes+
                " | interactiveSpend="+interactiveSpend;
    }

}
