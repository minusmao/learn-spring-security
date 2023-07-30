package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @PostMapping("/hello")
    @ResponseBody
    public String hello() {
        System.out.println("hello ok!");
        return "Hello ok";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

}

