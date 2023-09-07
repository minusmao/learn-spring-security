package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * 资源服务器<br>
 * 对应的授权服务器配置类是 {@link com.example.config.JdbcAuthorizationServerConfig}<br>
 *
 * @author minus
 * @since 2023-09-08 00:50
 */
//开启 oauth 资源服务器
@Configuration
@EnableResourceServer
@ConditionalOnMissingBean(RemoteResourceServerConfig.class)    // 个人补充：此注解是为了使当前配置类失效
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    private final DataSource dataSource;

    @Autowired
    public ResourceServerConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.tokenStore(tokenStore());
    }

    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

}
