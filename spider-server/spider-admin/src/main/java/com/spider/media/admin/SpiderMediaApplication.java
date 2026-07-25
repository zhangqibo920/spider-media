package com.spider.media.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spider Media 蜘蛛自媒体运营中台 - 启动类
 *
 * <p>Spring Boot 应用入口，启动所有模块的 Bean 扫描和自动配置。</p>
 *
 * <p>启用的功能：
 * <ul>
 *   <li>@ComponentScan - 扫描 com.spider.media 包下所有组件</li>
 *   <li>@MapperScan - 扫描各模块的 MyBatis Mapper 接口</li>
 *   <li>@EnableAsync - 启用异步方法执行（用于热点抓取、文章生成、内容发布等耗时操作）</li>
 *   <li>@EnableScheduling - 启用定时任务调度</li>
 * </ul></p>
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
