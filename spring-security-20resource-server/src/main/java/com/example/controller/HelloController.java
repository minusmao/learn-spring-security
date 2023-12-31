package com.example.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // 访问资源（将 access-token 放到头部）
    // 例如：curl -H "Authorization:Bearer dffa62d2-1078-457e-8a2b-4bd46fae0f47" http://localhost:8081/hello
    @GetMapping("/hello")
    public Authentication hello() {
        String hello = "hello resource service";
        System.out.println(hello);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication);
        return authentication;
    }

}
