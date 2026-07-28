package com.spider.media.hotmonitor.skill;

import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.hotmonitor.entity.HmKeyword;

import java.util.List;

public interface HmSkill {

    String skillName();

    void execute(HmKeyword keyword, List<AcHotTopic> allTopics);
}
