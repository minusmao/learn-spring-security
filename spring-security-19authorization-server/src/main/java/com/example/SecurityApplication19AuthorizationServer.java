package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 授权服务器
 *
 * @author minus
 * @since 2023-09-08 00:50
 */
@SpringBootApplication
public class SecurityApplication19AuthorizationServer {
    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication19AuthorizationServer.class, args);
    }

    /*
        OAuth2 标准接口：
            GET     /oauth/authorize        授权端点
            POST    /oauth/token            获取令牌端点
            /oauth/confirm_access   用户确认授权提交端点
            /oauth/error            授权服务错误信息端点
            /oauth/check_token      用于资源服务访问的令牌解析端点
            /oauth/token_key        提供公有密匙的端点，如果使用 JWT 令牌的话
     */

}