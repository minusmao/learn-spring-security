package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

/**
 * 授权服务器<br>
 * 客户端 id 和 secret 配置在内存中（直接代码写死，见 46 行）<br>
 * 貌似生成的 token 令牌也是存在内存中的（这里代码没配置InMemoryTokenStore()，应该是默认）<br>
 * 可以参考：<a href="https://mp.weixin.qq.com/s/GXMQI59U6uzmS-C0WQ5iUw">OAuth2 登录流程</a><br>
 * <br>
 * 因为是基于内存，所以资源服务器只能每次向授权服务器的/oauth/check_token接口发起请求，才能解析令牌<br>
 * 对应的资源服务器配置类是 {@link com.example.config.RemoteResourceServerConfig}<br>
 *
 * @author minus
 * @since 2023-09-08 00:50
 */
//自定义 授权服务器配置
@Configuration
@EnableAuthorizationServer //指定当前应用为授权服务器
//@ConditionalOnMissingBean(RedisAuthorizationServerConfig.class)    // 个人补充：此注解是为了使当前配置类失效
public class InMemoryAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public InMemoryAuthorizationServerConfig(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService, AuthenticationManager authenticationManager) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    //用来配置授权服务器可以为那些客户端授权  //id secret redirectURI 使用那种授权模式
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("client")// 客户端 id
                .secret(passwordEncoder.encode("secret"))//注册客户端秘钥（必须是加密后的密文）
                .redirectUris("http://www.baidu.com")
                .authorizedGrantTypes("authorization_code", "refresh_token", "implicit", "password", "client_credentials") //授权服务器支持的模式 仅支持授权码模式
                .scopes("read:user");//令牌允许获取的资源权限
    }

    //配置授权服务器使用哪个 userDetailService
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.userDetailsService(userDetailsService); //注入 userDetailService
        endpoints.authenticationManager(authenticationManager); //注入 authenticationManager
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

    //授权码这种模式:
    // 1.请求用户是否授权 /oauth/authorize
    // 完整路径: http://localhost:8080/oauth/authorize?client_id=client&response_type=code&redirect_uri=http://www.baidu.com
    // 2.授权之后根据获取的授权码获取令牌 /oauth/token  id secret redirectUri  授权类型: authorization_code
    // 完整路径: curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=authorization_code&code=IwvCtx&redirect_uri=http://www.baidu.com' "http://client:secret@localhost:8080/oauth/token"
    // 3.支持令牌刷新  /oauth/token  id  secret  授权类型 : refresh_token  刷新的令牌: refresh_token
    // 完整路径: curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=refresh_token&refresh_token=f6583d8a-598c-46bb-81d8-01fa6484cf05&client_id=client' "http://client:secret@localhost:8080/oauth/token"

}
