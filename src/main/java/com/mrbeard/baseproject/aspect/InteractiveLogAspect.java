package com.mrbeard.baseproject.aspect;

import com.alibaba.fastjson.JSON;
import com.mrbeard.baseproject.aspect.mapper.InteractiveLogMapper;
import com.mrbeard.baseproject.aspect.model.InteractiveLog;
import com.mrbeard.baseproject.blocks.authority.controller.AuthorityController;
import com.mrbeard.baseproject.blocks.authority.model.User;
import com.mrbeard.baseproject.exception.BaseRuntimeException;
import com.mrbeard.baseproject.util.SessionUtil;
import com.mrbeard.baseproject.util.ToolUtil;
import com.mrbeard.baseproject.util.WebUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Author mrbeard
 * @Date 2018/12/4 10:07
 * 操作交互日志AOP
 **/
@Component
@Aspect
public class InteractiveLogAspect {


    static Logger logger = LoggerFactory.getLogger(Scheduled.class);

    @Resource
    InteractiveLogMapper interactiveLogDao;
    @Resource
    AuthorityController authorityController;

    /**
     * 没有usertoken，排除记录用户信息的服务方法
     */
    private static String[] excludePathPatterns = new String[]{"register", "getRandomCode", "login", "sessionError","uploadImg","getImgByUrl"};

    /**
     * spring bean 单例模式
     * 为每个并发线程单独分配一个对象，用于处理请求过程的耗时
     * Thread 持有一个对象实例属性 ThreadLocal.ThreadLocalMap ,其实现用线性开放地址法hash来保存当前线程的变量，即各存各的，实现并发安全
     */
    private static ThreadLocal<Long> tlBeginTimePoint = new ThreadLocal<>();

    @Before(value = "@within(org.springframework.web.bind.annotation.RestController)")
    public void before() {
        tlBeginTimePoint.set(System.currentTimeMillis());
    }


    /**
     * 参数拦截aop
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around(value = "@within(org.springframework.web.bind.annotation.RestController)")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        StringBuffer sbf = new StringBuffer();
        //接口调用信息
        sbf.append("\n=========================================接口调用信息begin==============================================\n");
        sbf.append("调用ip:"+WebUtil.getRequest().getRemoteAddr()+"\n");
        sbf.append("调用端口:"+WebUtil.getRequest().getRemotePort()+"\n");
        //参数信息
        sbf.append("调用类：\t").append(pjp.getTarget().toString()).append("\n");
        Object[] os = pjp.getArgs();
        sbf.append("调用方法:\t").append(pjp.getSignature()).append("\n");
        sbf.append("参数:").append("\n");
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < os.length; i++) {
            sbf.append("\t==>input[").append(i).append("]:\t").append(os[i] == null ? "" : os[i].toString()).append("\n");
            stringBuffer.append(JSON.toJSONString(os[i]) + ",");
        }
        Object retVal = pjp.proceed();
        String retValStr = JSON.toJSONString(retVal);
        if (retVal != null && retValStr.length() < 2000) {
            sbf.append("Result:\t").append(retValStr == null ? "" : retValStr).append("\n");
        }
        sbf.append("===========================================接口调用信息end==============================================\n");
        logger.info(sbf.toString());
        return retVal;
    }

    @AfterReturning(value = "@within(org.springframework.web.bind.annotation.RestController)", returning = "proxyMethodReturn")
    public void afterReturning(JoinPoint joinPoint, Object proxyMethodReturn) {
        InteractiveLog interactiveLog = null;
        try {
            interactiveLog = getInteractiveLog(joinPoint);
            String responseMes = ToolUtil.isEmpty(proxyMethodReturn) ? "" : proxyMethodReturn.toString();
            interactiveLog.setResponseMes(responseMes);
            interactiveLogDao.insert(interactiveLog);
        } catch (Exception e) {
            //记录日志时发生异常，自己感知处理
            if(interactiveLog != null){
                logger.warn("InteractiveLog >>> " + interactiveLog == null ? "null" : interactiveLog.toString());
                throw new BaseRuntimeException(e.getMessage());
            }
        }
    }

    @AfterThrowing(value = "@within(org.springframework.web.bind.annotation.RestController)", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Throwable exception) {
        InteractiveLog interactiveLog = null;
        try {
            interactiveLog = getInteractiveLog(joinPoint);
            String exceptionMes = "Message:" + exception.getMessage() + " | Cause:" + exception.getCause();
            interactiveLog.setExceptionMes(exceptionMes);
            interactiveLogDao.insert(interactiveLog);
        } catch (Exception e) { //记录日志时发生异常，自己感知处理
            if(interactiveLog != null){
                logger.warn("InteractiveLog >>> " + interactiveLog == null ? "null" : interactiveLog.toString());
                throw new BaseRuntimeException(e.getMessage());
            }
        }
    }

    /**
     * 设置交互日志内容
     *
     * @param joinPoint
     * @return
     */
    private InteractiveLog getInteractiveLog(JoinPoint joinPoint) {
        StringBuilder sb = new StringBuilder();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            sb.append("arg" + i + ":");
            sb.append(JSON.toJSONString(args[i]));
            sb.append(" | ");
        }
        //当前请求用户信息
        String responseMethod = joinPoint.getSignature().getName();
        User user = null;
        for (String method : excludePathPatterns) {
            //被排除的请求路径方法
            if (responseMethod.equals(method)) {
                user = new User();
                break;
            }
        }
        //null 本次请求不在排除的路径中，需获取用户信息
        if (user == null) {
            user = SessionUtil.getUserInfo();
        }
        //构造交互日志对象
        InteractiveLog interactiveLog = new InteractiveLog();
        interactiveLog.setRequester(user.getUid());
        interactiveLog.setRequesterName(user.getUname());
        interactiveLog.setRequestTime(new Date());
        interactiveLog.setRequestParameter(sb.toString());
        interactiveLog.setResponseClass(joinPoint.getTarget().getClass().getName());
        interactiveLog.setResponseMethod(responseMethod);
        //本次请求响应花费的时间
        interactiveLog.setInteractiveSpend(System.currentTimeMillis() - tlBeginTimePoint.get());
        return interactiveLog;
    }
}
