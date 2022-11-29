package com.aqwx.diners;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@MapperScan("com.aqwx.diners.dao")
@EnableOpenApi
public class DinersApplication {
    public static void main(String[] args) {
        SpringApplication.run(DinersApplication.class, args);
    }
}
