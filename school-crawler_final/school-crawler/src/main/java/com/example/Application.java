package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")  
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
//讓程式開始的class

//看註解寫word，前端也要看