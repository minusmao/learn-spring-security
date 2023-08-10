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

//自定义 授权服务器配置
@Configuration
@EnableAuthorizationServer //指定当前应用为授权服务器
@ConditionalOnMissingBean(RedisAuthorizationServerConfig.class)    // 个人补充：此注解是为了使当前配置类失效
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

    //授权码这种模式:
    // 1.请求用户是否授权 /oauth/authorize
    // 完整路径: http://localhost:8080/oauth/authorize?client_id=client&response_type=code&redirect_uri=http://www.baidu.com
    // 2.授权之后根据获取的授权码获取令牌 /oauth/token  id secret redirectUri  授权类型: authorization_code
    // 完整路径: curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=authorization_code&code=IwvCtx&redirect_uri=http://www.baidu.com' "http://client:secret@localhost:8080/oauth/token"
    // 3.支持令牌刷新  /oauth/token  id  secret  授权类型 : refresh_token  刷新的令牌: refresh_token
    // 完整路径: curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=refresh_token&refresh_token=f6583d8a-598c-46bb-81d8-01fa6484cf05&client_id=client' "http://client:secret@localhost:8080/oauth/token"

}
