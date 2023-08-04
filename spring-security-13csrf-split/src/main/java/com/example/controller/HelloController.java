package com.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @PostMapping("/hello")
    public String hello() {
        System.out.println("hello");
        return "hello";
    }

    @PostMapping("/withdraw")
    public String withdraw() {
        System.out.println("执行一次转账操作");
        return "执行一次转账操作";
    }

}