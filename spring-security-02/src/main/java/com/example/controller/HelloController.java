package com.example.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * HelloController
 * @author minus
 * @since 2023-07-06 11:47
 */
@RestController
public class HelloController {

    @PostConstruct
    public void enableAuthCtxOnSpawnedThreads() {
        // 设置 SecurityContextHolder 的模式，使 SecurityContext 支持子线程
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        // 也可以使用另一种方法：增加 VM Options 参数 -Dspring.security.strategy=MODE_INHERITABLETHREADLOCAL
    }

    @GetMapping("/hello")
    public String hello() {
        System.out.println("hello security");
        //1.获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        System.out.println("身份 :"+principal.getUsername());
        System.out.println("凭证 :"+authentication.getCredentials());
        System.out.println("权限 :"+authentication.getAuthorities());
        new Thread(() -> {
            System.out.println("子线程获取");
            Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal1 = (UserDetails) authentication1.getPrincipal();
            System.out.println("身份 :"+principal1.getUsername());
            System.out.println("凭证 :"+authentication1.getCredentials());
            System.out.println("权限 :"+authentication1.getAuthorities());
        }).start();
        return "hello security";
    }

}
