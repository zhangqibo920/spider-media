package com.spider.media.hotmonitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HmEmailService {

    private static final Logger log = LoggerFactory.getLogger(HmEmailService.class);
    private final JavaMailSender mailSender;
    private final boolean enabled;

    public HmEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        this.mailSender = sender;
        this.enabled = sender != null;
        if (!enabled) {
            log.warn("JavaMailSender 未配置（缺少 spring.mail.host），邮件通知已禁用");
        }
    }

    public void sendNotification(String to, String keyword, List<String> topicTitles, String topTitle, int total) {
        if (!enabled) return;
        try {
            var msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("热点监控: " + keyword);

            StringBuilder sb = new StringBuilder();
            sb.append("<h3>热点监控通知</h3>");
            sb.append("<p>关键词 <b>").append(keyword).append("</b> 发现 ").append(total).append(" 条相关热点</p>");
            sb.append("<p>最高热度: <b>").append(topTitle).append("</b></p>");
            if (!topicTitles.isEmpty()) {
                sb.append("<ul>");
                for (String t : topicTitles) {
                    sb.append("<li>").append(t).append("</li>");
                }
                sb.append("</ul>");
            }
            helper.setText(sb.toString(), true);
            mailSender.send(msg);
            log.info("邮件通知已发送到: {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败 to={}", to, e);
        }
    }
}
