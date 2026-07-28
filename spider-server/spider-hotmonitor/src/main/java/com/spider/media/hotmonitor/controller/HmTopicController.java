package com.spider.media.hotmonitor.controller;

import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.common.controller.BaseController;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import com.spider.media.hotmonitor.service.IHmMonitorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotmonitor/topic")
public class HmTopicController extends BaseController {

    private final IHmMonitorService monitorService;

    public HmTopicController(IHmMonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GetMapping("/list")
    public R<List<AcHotTopic>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer minRelevance,
            @RequestParam(defaultValue = "hot_score") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        Long userId = LoginUser.getUserId();
        return list(monitorService.queryTopics(userId, keyword, source, minScore, minRelevance, sortBy, sortOrder));
    }

    @GetMapping("/by-keyword/{keywordId}")
    public R<List<AcHotTopic>> byKeyword(@PathVariable Long keywordId) {
        Long userId = LoginUser.getUserId();
        return list(monitorService.fetchByKeyword(keywordId, userId));
    }
}
