package com.aqwx.feeds;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aqwx.feeds.dao")
public class FeedsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedsApplication.class, args);
    }
}
