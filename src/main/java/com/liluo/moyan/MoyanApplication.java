package com.liluo.moyan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan(basePackages = "com.liluo.moyan", annotationClass = org.apache.ibatis.annotations.Mapper.class)
@EnableCaching
public class MoyanApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoyanApplication.class, args);
    }

}
