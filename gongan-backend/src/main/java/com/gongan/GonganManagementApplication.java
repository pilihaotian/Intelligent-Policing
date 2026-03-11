package com.gongan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gongan.mapper")
public class GonganManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(GonganManagementApplication.class, args);
    }
}