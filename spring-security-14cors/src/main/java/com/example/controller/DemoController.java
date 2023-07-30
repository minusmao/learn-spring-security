package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@CrossOrigin //代表类中所有方法允许跨域  springmvc 注解解决方案
public class DemoController {

    @GetMapping("/demo")
    // @CrossOrigin(origins = {"http://127.0.0.1:63342"}) //用来解决允许跨域访问注解
    //@CrossOrigin
    public String demo() {
        System.out.println("demo ok!");
        return "demo ok!";
    }

}
