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

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/spider_media?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai",
            "root", "123456"
        );
    }

    private long countTable(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

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
