package com.base.project.config;

import cn.hutool.core.util.StrUtil;
import com.base.project.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 *
 * @author mrbeard
 * @date 2020/08/25
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {


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
        log.info("========================RequestUrl" + request.getRequestURL().toString());
        log.info("========================LoginInterceptor");
        //从request中获取到usertoken
        String usertoken = request.getParameter("logintoken");
        //如果usertoken为空,返回错误。
      /**  if (StrUtil.isEmpty(usertoken)) {
            //用户未登录，或session已经失效,重定向到错误类中进行处理
            String requestUrl = request.getRequestURL().toString().trim();
            String servletPath = request.getServletPath();
            //去除servletPath
            requestUrl = requestUrl.replace(servletPath, "");
            log.error("{LoginInterceptor}====>logintoken为空");
            throw new CommonException("用户session已失效，请退出后重新登陆刷新！");
        }*/
        // redis中获取到了对应的值则重新刷新session有效时间
        //刷新session有效时间
        /**
         * 为方便接口调试阶段，不用先进行登录操作才能通过拦截器并进行接口调用，
         * 这里判断redis中是否存有key为userToken_mockUser的数据，如果有，则认为是开发或测试的模拟环境
         * 并将userToken_mockUser对应的value中的内容视为模拟的用户的session数据
         * 如果不需要这个模拟，则在redis中去除此条数据即可
         */
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

}
