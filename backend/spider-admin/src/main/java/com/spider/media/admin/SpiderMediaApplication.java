package com.spider.media.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spider Media 启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.spider.media")
@MapperScan("com.spider.media.*.mapper")
@EnableAsync
@EnableScheduling
public class SpiderMediaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpiderMediaApplication.class, args);
        System.out.println("====================================");
        System.out.println("  蜘蛛自媒体运营中台启动成功！");
        System.out.println("  Spider Media v1.0.0");
        System.out.println("====================================");
    }
}
