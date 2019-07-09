package com.mrbeard.baseproject.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mrbeard.baseproject.blocks.authority.model.User;
import com.mrbeard.baseproject.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author mrbeard
 * @date 2018-10-15 16:57
 * @description
 */
@Component
public class SessionUtil {

    private static Logger logger = LoggerFactory.getLogger(SessionUtil.class);

    /**
     * 根据usertoken获取redis中的用户session信息
     * @return
     */
    public static User getUserInfo() throws BaseRuntimeException {
        //logintoken
        String logintoken = WebUtil.getRequest().getParameter("logintoken");
        User user = null;
        try {
            if(ToolUtil.isEmpty(logintoken)){
                WebUtil.getResponse().sendRedirect(WebUtil.getRequest().getContextPath() + "/sessionError");
            }
            //根据usertoken取session信息
            String userInfo = JedisUtil.get("logintoken"+logintoken);

            Map<String, Object> userInfoMap = JSON.parseObject(userInfo, new TypeReference<Map<String, Object>>() {});
            user = JSON.parseObject(String.valueOf(userInfoMap.get("userInfo")), User.class);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            throw new BaseRuntimeException(e.getMessage());
        }
        return user;
    }
}
