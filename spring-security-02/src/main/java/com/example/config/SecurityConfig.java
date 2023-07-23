package com.example.config;

import com.example.handler.MyAuthenticationFailureHandler;
import com.example.handler.MyAuthenticationSuccessHandler;
import com.example.handler.MyLogoutSuccessHandler;
import com.example.service.MyUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

/**
 * spring-security 配置类
 *
 * @author minus
 * @since 2023-07-23 14:16
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final MyUserDetailService myUserDetailService;

    @Autowired
    public SecurityConfig(MyUserDetailService myUserDetailService) {
        this.myUserDetailService = myUserDetailService;
    }

    //    @Bean
//    public UserDetailsService userDetailsService() {
//        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
//        userDetailsService.createUser(User.withUsername("aaa").password("{noop}123").roles("admin").build());
//        return userDetailsService;
//    }

    //springboot 对 security 默认配置中  在工厂中默认创建 AuthenticationManager
//    @Autowired
//    public void initialize(AuthenticationManagerBuilder builder) throws Exception {
//        System.out.println("springboot 默认配置: " + builder);
//    }

    //自定义AuthenticationManager  推荐  并没有在工厂中暴露出来
    @Override
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        System.out.println("自定义AuthenticationManager: " + builder);
        builder.userDetailsService(myUserDetailService);
    }

    //作用: 用来将自定义AuthenticationManager在工厂中进行暴露,可以在任何位置注入
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/index").permitAll()    // 设置白名单
                .mvcMatchers("/login.html").permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/doLogin") // 默认 login
                .usernameParameter("uname")     // 默认 username
                .passwordParameter("passwd")    // 默认 password
                //.successForwardUrl("/index")  // 认证成功    forward 跳转      注意:不会跳转到之前请求路径
                //.defaultSuccessUrl("/index")  // 认证成功    redirect 重定向    注意:如果之前请求路径,会有优先跳转之前请求路径
                .successHandler(new MyAuthenticationSuccessHandler()) // 认证成功  处理器  前后端分离解决方案
                //.failureUrl("/login.html")           // 认证失败  redirect 重定向
                //.failureForwardUrl("/login.html")    // 认证失败  forward 跳转，因需获取 request 中异常信息，这里只能使用 failureForwardUrl
                .failureHandler(new MyAuthenticationFailureHandler()) // 认证失败  处理器  前后端分离解决方案
                .and()
                .logout()
                //.logoutUrl("/logout")  // 指定注销登录 url，默认请求方式必须: GET
                .logoutRequestMatcher(new OrRequestMatcher(
                        // 配置多个注销登录 url
                        new AntPathRequestMatcher("/aa", "GET"),
                        new AntPathRequestMatcher("/bb", "POST")
                ))
                .invalidateHttpSession(true) //默认 会话失效
                .clearAuthentication(true)   //默认 清楚认证标记
                //.logoutSuccessUrl("/login.html") //注销登录 成功之后跳转页面
                .logoutSuccessHandler(new MyLogoutSuccessHandler())
                .and()
                .csrf().disable();//这里先关闭 CSRF

    }

}
