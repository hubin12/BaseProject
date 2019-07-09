package com.mrbeard.baseproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @ClassName WebSocketConfig
 * @Description 开启WebSocket支持
 * @Author Mrbeard
 * @Date 2019/1/31 11:04
 * @Version 1.0
 **/
//@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
