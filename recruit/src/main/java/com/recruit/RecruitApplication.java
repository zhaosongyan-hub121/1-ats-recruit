package com.recruit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 招聘管理后端启动类
 * - @SpringBootApplication 开启 SpringBoot 自动装配
 * - @MapperScan            扫描 MyBatis-Plus Mapper 接口包
 */
@SpringBootApplication
@MapperScan("com.recruit.**.mapper")
public class RecruitApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitApplication.class, args);
    }

}
