package com.flink;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@MapperScan("com.flink.mapper")
@ComponentScan(basePackages = {"com.flink.*"})
public class SpringbootFlinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootFlinkApplication.class, args);
    }

}
