package com.mrbeard.baseproject.interceptor;

import com.mrbeard.baseproject.common.Constant;
import com.mrbeard.baseproject.exception.BaseRuntimeException;
import com.mrbeard.baseproject.util.JedisUtil;
import com.mrbeard.baseproject.util.ToolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author mrbeard
 * @Date 2018/11/26 15:01
 * 登录拦截器
 **/
public class LoginInterceptor implements HandlerInterceptor {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 请求进入到正式业务层之前进行拦截判断是登陆或session已经过期
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("========================RequestUrl"+request.getRequestURL().toString());
        logger.info("========================LoginInterceptor");
        //从request中获取到usertoken
        String usertoken = request.getParameter("logintoken");
        //如果usertoken为空,返回错误。
        if(ToolUtil.isEmpty(usertoken)){
            //用户未登录，或session已经失效,重定向到错误类中进行处理
            String requestUrl = request.getRequestURL().toString().trim();
            String servletPath = request.getServletPath();
            //去除servletPath
            requestUrl = requestUrl.replace(servletPath,"");
            logger.error("{LoginInterceptor}====>logintoken为空");
//            response.sendRedirect(requestUrl + "/errorPage");
            throw new BaseRuntimeException("用户session已失效，请退出后重新登陆刷新！");
        }
        // redis中获取到了对应的值则重新刷新session有效时间
        if (ToolUtil.isNotEmpty(JedisUtil.get("logintoken_"+usertoken))) {
            //刷新session有效时间
            JedisUtil.add("logintoken_"+usertoken, JedisUtil.get("logintoken_"+usertoken), Constant.USER_TOKEN_INVALIDTIME);
            return true;
        } else {
            /**
             * 为方便接口调试阶段，不用先进行登录操作才能通过拦截器并进行接口调用，
             * 这里判断redis中是否存有key为userToken_mockUser的数据，如果有，则认为是开发或测试的模拟环境
             * 并将userToken_mockUser对应的value中的内容视为模拟的用户的session数据
             * 如果不需要这个模拟，则在redis中去除此条数据即可
             */
            if(ToolUtil.isNotEmpty(JedisUtil.get("usertoken_mockUser"))){
                return true;
            }
            //用户未登录，或session已经失效,重定向到错误类中进行处理
            String requestUrl = request.getRequestURL().toString().trim();
            String servletPath = request.getServletPath();
            //去除servletPath
            requestUrl = requestUrl.replace(servletPath,"");
            logger.error("{LoginInterceptor}====>session为空");
//            response.sendRedirect(requestUrl + "/errorPage");
            throw new BaseRuntimeException("用户session已失效，请退出后重新登陆刷新！");
        }
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

}
