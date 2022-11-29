package com.aqwx.diners.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* test */
@RestController
@RequestMapping("hello")
public class HelloController {

    @RequestMapping("word")
    public String hello(String name){
        return "hello" + name;
    }
}
