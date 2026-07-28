package com.spider.media.hotmonitor.service;

import com.spider.media.system.service.ISysConfigService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
public class HmEmailService {

    private static final Logger log = LoggerFactory.getLogger(HmEmailService.class);
    private final JavaMailSender mailSender;
    private final boolean enabled;

    public HmEmailService(ISysConfigService configService) {
        String host = configService.getConfigValueByKey("smtp.host");
        if (host == null || host.isBlank()) {
            log.warn("SMTP 未配置（sys_config 中缺少 smtp.host），邮件功能已禁用");
            this.mailSender = null;
            this.enabled = false;
            return;
        }
        String portStr = configService.getConfigValueByKey("smtp.port", "587");
        String username = configService.getConfigValueByKey("smtp.username", "");
        String password = configService.getConfigValueByKey("smtp.password", "");
        String from = configService.getConfigValueByKey("smtp.from", username);

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(Integer.parseInt(portStr));
        if (!username.isBlank()) sender.setUsername(username);
        if (!password.isBlank()) sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        this.mailSender = sender;
        this.enabled = true;
        log.info("SMTP 已配置: host={}, port={}, from={}", host, portStr, from);
    }

    public void sendNotification(String to, String keyword, List<String> topicTitles, String topTitle, int total) {
        if (!enabled) return;
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
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
