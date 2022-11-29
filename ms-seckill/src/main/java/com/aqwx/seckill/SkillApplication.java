package com.aqwx.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aqwx.seckill.dao")
public class SkillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillApplication.class, args);
    }
}
