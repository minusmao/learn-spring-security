package com.example.config;

import com.example.service.MyUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final MyUserDetailService myUserDetailService;

    @Autowired
    public WebSecurityConfig(MyUserDetailService myUserDetailService) {
        this.myUserDetailService = myUserDetailService;
    }

    //使用 passwordEncoder 第二种方式（直接指定系统的密码加密方式）
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }


    //第一种 passwordEncoder 使用方式（密码加密方式由密码前面的{***}指定）
//    @Bean
//    public UserDetailsService userDetailsService() {
//        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();
//        //inMemoryUserDetailsManager.createUser(User.withUsername("root").password("{bcrypt}$2a$10$.kKfTxVyEBT.OMb/VxQCU.FHRfzkrbHBUMKEgtKkuR8uBhh8JbqUm").roles("admin").build());
//        inMemoryUserDetailsManager.createUser(User.withUsername("root").password("$2a$10$.kKfTxVyEBT.OMb/VxQCU.FHRfzkrbHBUMKEgtKkuR8uBhh8JbqUm").roles("admin").build());
//        return inMemoryUserDetailsManager;//{}
//    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .and()
                .csrf().disable();
    }
}
