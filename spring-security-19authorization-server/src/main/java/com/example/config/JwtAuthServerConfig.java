package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
public class JwtAuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private UserDetailsService userDetailsService;

    @Bean // 声明 ClientDetails 实现
    public ClientDetailsService clientDetails() {
        JdbcClientDetailsService jdbcClientDetailsService = new JdbcClientDetailsService(dataSource);
        jdbcClientDetailsService.setPasswordEncoder(passwordEncoder);
        return jdbcClientDetailsService;
    }

    @Override//使用数据库方式客户端存储
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetails());
    }

    @Bean//使用 JWT 方式生成令牌
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean//使用同一个密钥来编码 JWT 中的  OAuth2 令牌
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("123");//可以采用属性注入方式 生产中建议加密
        return converter;
    }

    @Override //配置使用 jwt 方式颁发令牌,同时配置 jwt 转换器
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.tokenStore(tokenStore())//配置令牌存储为数据库存储
                .accessTokenConverter(jwtAccessTokenConverter())
                .authenticationManager(authenticationManager)//认证管理器
                .userDetailsService(userDetailsService);// 不添加则 refresh token 的时候会报错
    }

    //授权码这种模式:
    // 1.请求用户是否授权 /oauth/authorize（注：客户端信息，如 client_id、client_secret 在 oauth_client_token 表中）
    // 完整路径: http://localhost:8080/oauth/authorize?client_id=client&response_type=code&redirect_uri=http://www.baidu.com
    // 2.授权之后根据获取的授权码获取令牌 /oauth/token  id secret redirectUri  授权类型: authorization_code
    // 完整路径: curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=authorization_code&code=IwvCtx&redirect_uri=http://www.baidu.com' "http://client:secret@localhost:8080/oauth/token"
    // 3.支持令牌刷新  /oauth/token  id  secret  授权类型 : refresh_token  刷新的令牌: refresh_token
    // 完整路径: curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=refresh_token&refresh_token=f6583d8a-598c-46bb-81d8-01fa6484cf05&client_id=client' "http://client:secret@localhost:8080/oauth/token"

}
