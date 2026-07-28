package com.spider.media.hotmonitor.skill.impl;

import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.hotmonitor.entity.HmKeyword;
import com.spider.media.hotmonitor.service.HmEmailService;
import com.spider.media.hotmonitor.skill.HmSkill;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmailSkill implements HmSkill {

    private final HmEmailService emailService;

    public EmailSkill(HmEmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public String skillName() {
        return "email";
    }

    @Override
    public void execute(HmKeyword keyword, List<AcHotTopic> allTopics) {
        if (!"1".equals(keyword.getNotifyEmail()) || keyword.getNotifyEmailAddr() == null || keyword.getNotifyEmailAddr().isBlank()) {
            return;
        }
        if (allTopics.isEmpty()) return;

        AcHotTopic top = allTopics.stream()
                .max(Comparator.comparingInt(AcHotTopic::getHotScore))
                .orElse(allTopics.get(0));
        List<String> titles = allTopics.stream()
                .map(AcHotTopic::getTitle)
                .collect(Collectors.toList());

        emailService.sendNotification(
                keyword.getNotifyEmailAddr(),
                keyword.getKeyword(),
                titles,
                top.getTitle(),
                allTopics.size()
        );
    }
}
