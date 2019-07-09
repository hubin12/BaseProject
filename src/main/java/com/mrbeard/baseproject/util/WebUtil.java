package com.mrbeard.baseproject.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通用工具类
 * 
 * @author mrbeard
 * @date 2018年8月18日
 */
public class WebUtil {

	static Logger logger = LoggerFactory.getLogger(WebUtil.class);

	/**
	 * @description 获取HTTP请求
	 * @author rico
	 * @created 2017年7月4日 下午5:18:08
	 * @return
	 */
	public static HttpServletRequest getRequest() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		return request;
	}

	public static HttpServletResponse getResponse() {
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
		return response;
	}

}
