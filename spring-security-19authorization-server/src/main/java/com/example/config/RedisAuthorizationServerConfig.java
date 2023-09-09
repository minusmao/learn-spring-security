package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;

/**
 * 授权服务器<br>
 * 客户端 id 和 secret 配置在数据库中（见 89 行）<br>
 * 生成的 token 令牌也是存在 redis 中的（见 65、100 行）<br>
 * 可以参考：<a href="https://mp.weixin.qq.com/s/cGopy8hDPtkn8Q7HUYabbA">OAuth2 令牌还能存入 Redis</a>
 * <p>
 * 对应的资源服务器配置类是 {@link com.example.config.RedisResourceServerConfig}<br>
 *
 * @author minus
 * @since 2023-09-08 00:50
 */
@Configuration
@EnableAuthorizationServer
@ConditionalOnMissingBean(JwtAuthServerConfig.class)    // 个人补充：此注解是为了使当前配置类失效
public class RedisAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Autowired
    DataSource dataSource;  // 客户端信息存数据库中

    @Bean// 存储 token 的方式
    public TokenStore tokenStore() {
        return new RedisTokenStore(redisConnectionFactory);
    }

    /**
     * 用于客户端信息访问的 Service
     * @return ClientDetailsService
     */
    @Bean
    ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    /**
     * 配置 token 的具体属性
     * @return AuthorizationServerTokenServices
     */
    @Bean
    AuthorizationServerTokenServices tokenServices() {
        DefaultTokenServices services = new DefaultTokenServices();
        services.setClientDetailsService(clientDetails());  // 用于客户端信息访问的 Service
        services.setSupportRefreshToken(true);  // token 是否支持刷新
        services.setTokenStore(tokenStore());  // token 的存储位置
        return services;
    }

    /**
     * 配置令牌端点的安全约束，也就是/oauth/check_token这个端点谁能访问，谁不能访问
     * @param security a fluent configurer for security features
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.checkTokenAccess("permitAll()")  // checkTokenAccess 是指一个 token 校验的端点，这个端点我们设置为可以直接访问
                .allowFormAuthenticationForClients();
    }

    /**
     * 配置用于客户端的信息访问的 Service（客户端的信息我们可以存在数据库中，这其实也是比较容易的，和用户信息存到数据库中类似）
     * @param clients the client details configurer
     * @throws Exception 异常
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetails());  // 设置用于客户端信息访问的 Service
    }

    /**
     * 配置令牌的访问端点和令牌服务
     * @param endpoints the endpoints configurer
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authorizationCodeServices(authorizationCodeServices())  // 配置授权码 code 的存储
                .tokenServices(tokenServices());  // 配置 token 的存储
    }

    /**
     * 授权码 code 的存储方案
     * @return AuthorizationCodeServices
     */
    @Bean
    AuthorizationCodeServices authorizationCodeServices() {
        return new InMemoryAuthorizationCodeServices();
    }

}
