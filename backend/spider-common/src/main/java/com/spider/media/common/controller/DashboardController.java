package com.spider.media.common.controller;

import com.spider.media.common.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘统计控制器
 *
 * <p>提供首页仪表盘所需的统计数据接口，包括各模块的数据量统计。
 * 通过直连数据库查询各表的有效记录数，为前端仪表盘展示提供数据支持。</p>
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    /**
     * 获取数据库连接
     *
     * @return MySQL 数据库连接对象
     * @throws Exception 连接失败时抛出异常
     */
    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/spider_media?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai",
            "root", "123456"
        );
    }

    /**
     * 执行 COUNT 查询并返回结果
     *
     * @param sql 统计 SQL 语句
     * @return 查询到的数量，查询失败时返回 0
     */
    private long countTable(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            // 查询异常时返回 0，不影响整体统计
        }
        return 0;
    }

    /**
     * 获取仪表盘统计数据
     *
     * <p>返回各模块的有效数据量统计，用于首页仪表盘的数字展示。
     * 包括：对标账号数、采集文章数、AI生成文章数、已发布任务数、热点话题数、发布账号数、定时任务数。</p>
     *
     * @return 包含各模块统计数据的 Map
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("targetAccounts", countTable("SELECT COUNT(*) FROM dc_target_account WHERE del_flag='0'"));
        stats.put("articles", countTable("SELECT COUNT(*) FROM dc_collected_article WHERE del_flag='0'"));
        stats.put("aiGenerated", countTable("SELECT COUNT(*) FROM ac_generated_article WHERE del_flag='0'"));
        stats.put("published", countTable("SELECT COUNT(*) FROM pb_publish_task WHERE status IN (1,2) AND del_flag='0'"));
        stats.put("hotTopics", countTable("SELECT COUNT(*) FROM ac_hot_topic WHERE del_flag='0'"));
        stats.put("publishAccounts", countTable("SELECT COUNT(*) FROM pb_platform_account WHERE del_flag='0'"));
        stats.put("scheduledTasks", countTable("SELECT COUNT(*) FROM ts_scheduled_task WHERE del_flag='0'"));
        return R.ok(stats);
    }
}
