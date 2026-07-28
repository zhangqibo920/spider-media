package com.spider.media.hotmonitor.skill;

import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.hotmonitor.entity.HmKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HmSkillEngine {

    private static final Logger log = LoggerFactory.getLogger(HmSkillEngine.class);
    private final List<HmSkill> skills;

    public HmSkillEngine(List<HmSkill> skills) {
        this.skills = skills;
    }

    public void executeAll(HmKeyword keyword, List<AcHotTopic> allTopics) {
        for (HmSkill skill : skills) {
            try {
                skill.execute(keyword, allTopics);
            } catch (Exception e) {
                log.error("Skill执行失败: {}", skill.skillName(), e);
            }
        }
    }
}
