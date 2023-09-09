package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * 授权服务器<br>
 * 客户端 id 和 secret 配置在数据库中（见 54 行）<br>
 * 生成的 token 令牌也是存在数据库中的（见 62、72 行）<br>
 * 对应的资源服务器配置类是 {@link com.example.config.ResourceServerConfig}<br>
 *
 * @author minus
 * @since 2023-09-08 00:50
 */
@Configuration
@EnableAuthorizationServer
@ConditionalOnMissingBean(JwtAuthServerConfig.class)    // 个人补充：此注解是为了使当前配置类失效
public class JdbcAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public JdbcAuthorizationServerConfig(DataSource dataSource, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Bean
    public ClientDetailsService clientDetails() {
        JdbcClientDetailsService jdbcClientDetailsService = new JdbcClientDetailsService(dataSource);
        jdbcClientDetailsService.setPasswordEncoder(passwordEncoder);
        return jdbcClientDetailsService;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 配置管理 Client 客户端信息的 Service
        clients.withClientDetails(clientDetails());
    }

    //配置令牌存储
    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager);//认证管理器
        endpoints.tokenStore(tokenStore());//配置令牌存储为数据库存储

        // 配置TokenServices参数
        DefaultTokenServices tokenServices = new DefaultTokenServices();//修改默认令牌生成服务
        tokenServices.setTokenStore(endpoints.getTokenStore());//基于数据库令牌生成
        tokenServices.setSupportRefreshToken(true);//是否支持刷新令牌
        tokenServices.setReuseRefreshToken(true);//是否重复使用刷新令牌（直到过期）

        tokenServices.setClientDetailsService(endpoints.getClientDetailsService());//设置客户端信息
        tokenServices.setTokenEnhancer(endpoints.getTokenEnhancer());//用来控制令牌存储增强策略
        //访问令牌的默认有效期（以秒为单位）。过期的令牌为零或负数。
        tokenServices.setAccessTokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(30)); // 30天
        //刷新令牌的有效性（以秒为单位）。如果小于或等于零，则令牌将不会过期
        tokenServices.setRefreshTokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(3)); //3天

        endpoints.tokenServices(tokenServices);//使用配置令牌服务
    }

    //授权码这种模式:
    // 1.请求用户是否授权 /oauth/authorize（注：客户端信息，如 client_id、client_secret 在 oauth_client_token 表中）
    // 完整路径: http://localhost:8080/oauth/authorize?client_id=client&response_type=code&redirect_uri=http://www.baidu.com
    // 2.授权之后根据获取的授权码获取令牌 /oauth/token  id secret redirectUri  授权类型: authorization_code
    // 完整路径: curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=authorization_code&code=IwvCtx&redirect_uri=http://www.baidu.com' "http://client:secret@localhost:8080/oauth/token"
    // 3.支持令牌刷新  /oauth/token  id  secret  授权类型 : refresh_token  刷新的令牌: refresh_token
    // 完整路径: curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=refresh_token&refresh_token=f6583d8a-598c-46bb-81d8-01fa6484cf05&client_id=client' "http://client:secret@localhost:8080/oauth/token"

}
