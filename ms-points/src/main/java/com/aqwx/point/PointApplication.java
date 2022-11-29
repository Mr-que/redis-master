package com.aqwx.point;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aqwx.point.dao")
public class PointApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointApplication.class, args);
    }
}
