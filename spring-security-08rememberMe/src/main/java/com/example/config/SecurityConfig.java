package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.util.UUID;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;

    @Autowired
    public SecurityConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();
        inMemoryUserDetailsManager.createUser(User.withUsername("root").password("{noop}123").roles("ADMIN").build());
        return inMemoryUserDetailsManager;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //.mvcMatchers("/index").rememberMe()  //指定资源记住我
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .and()
                .rememberMe() //开启记住我功能
                .tokenRepository(persistentTokenRepository())
                .rememberMeServices(rememberMeServices()) //指定 rememberService 实现
                //.rememberMeParameter("remember-me") 用来接收请求中哪个参数作为开启记住我的参数
                //.alwaysRemember(true) //总是记住我
                .and()
                .csrf().disable();
    }


    //指定数据库持久化
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        jdbcTokenRepository.setCreateTableOnStartup(false);//启动创建表结构
        return jdbcTokenRepository;
    }

    /*
      实现方式 1（默认）：TokenBasedRememberMeServices
        安全隐患较大：
          1. cookie 中保存了用户信息
          2. 只要有这个 cookie 都可以直接自动登录（只要没到过期时间）
      实现方式 2（更安全）：PersistentTokenBasedRememberMeServices
        对安全隐患进行了优化：
          1. cookie 中没有保存用户信息（信息保存在服务端，服务端可通过 cookie 中的 series 取出 PersistentRememberMeToken 对象）
          2. 每次登录都会刷新 token，即生成新的 PersistentRememberMeToken 对象。（当其他人使用 cookie 登录后，token 刷新，原用户无法自动登录，于是重新登录，又刷新了 token，这样他人就无法拿之前的 cookie 登录了）
    */


    //指定记住我的实现
    // 指定为方式 2
    @Bean
    public RememberMeServices rememberMeServices() {
        // 内存存储方式
//        return new PersistentTokenBasedRememberMeServices(
//                "key",                          //参数 1: 自定义一个生成令牌 key 默认 UUID
//                userDetailsService(),                //参数 2:认证数据源
//                new InMemoryTokenRepositoryImpl()    //参数 3:令牌存储方式
//        );

        // 持久化存储方式
        return new PersistentTokenBasedRememberMeServices(
                UUID.randomUUID().toString(),
                userDetailsService(),
                persistentTokenRepository()
        );
    }

}
