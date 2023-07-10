package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HelloController
 * @author minus
 * @since 2023-07-06 11:47
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        System.out.println("hello security");
        return "hello security";
    }

}
