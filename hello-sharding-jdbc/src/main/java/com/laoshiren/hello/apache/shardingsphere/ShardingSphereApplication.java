package com.laoshiren.hello.apache.shardingsphere;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ProjectName:     hello-apache-shardingsphere
 * Package:         com.laoshiren.hello.apache.shardingsphere
 * ClassName:       ShardingSphereApplication
 * Author:          laoshiren
 * Git:             xiangdehua@pharmakeyring.com
 * Description:
 * Date:            2020/6/30 17:41
 * Version:         1.0.0
 */
@SpringBootApplication
@MapperScan(basePackages = "com.laoshiren.hello.apache.shardingsphere.mapper")
public class ShardingSphereApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShardingSphereApplication.class,args);
    }

}
