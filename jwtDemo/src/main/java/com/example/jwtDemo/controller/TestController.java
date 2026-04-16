package com.example.jwtDemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, public endpoint!";
    }

    @GetMapping("/hi")
    public String hi() {
        return "Hi, protected endpoint!";
    }
}