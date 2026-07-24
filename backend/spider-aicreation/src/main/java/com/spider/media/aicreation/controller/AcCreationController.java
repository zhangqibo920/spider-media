package com.spider.media.aicreation.controller;

import com.spider.media.aicreation.controller.vo.AcGeneratedArticlePageReqVO;
import com.spider.media.aicreation.entity.AcGeneratedArticle;
import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.aicreation.service.IAcGeneratedArticleService;
import com.spider.media.aicreation.service.IAcHotTopicService;
import com.spider.media.common.controller.BaseController;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.R;
import com.spider.media.framework.security.LoginUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI创作Controller
 */
@RestController
@RequestMapping("/api/ai")
public class AcCreationController extends BaseController {

    private final IAcHotTopicService hotTopicService;
    private final IAcGeneratedArticleService generatedArticleService;

    public AcCreationController(IAcHotTopicService hotTopicService,
                                 IAcGeneratedArticleService generatedArticleService) {
        this.hotTopicService = hotTopicService;
        this.generatedArticleService = generatedArticleService;
    }

    /**
     * 抓取热点话题
     */
    @PostMapping("/hotTopic/fetch")
    public R<Void> fetchHotTopics(@RequestParam(value = "platform") String platform) {
        Long userId = LoginUser.getUserId();
        hotTopicService.fetchHotTopics(platform, userId);
        return ok();
    }

    /**
     * 查询热点列表
     */
    @GetMapping("/hotTopic/list")
    public R<List<AcHotTopic>> listHotTopics() {
        Long userId = LoginUser.getUserId();
        return list(hotTopicService.selectHotTopicList(userId));
    }

    /**
     * 生成文章
     */
    @PostMapping("/article/generate")
    public R<AcGeneratedArticle> generateArticle(@RequestBody Map<String, Object> request) {
        Long hotTopicId = Long.valueOf(request.get("hotTopicId").toString());
        String model = (String) request.getOrDefault("model", "deepseek");
        Long userId = LoginUser.getUserId();
        AcGeneratedArticle article = generatedArticleService.generateArticle(hotTopicId, userId, model);
        return ok(article);
    }

    /**
     * 查询文章分页列表
     */
    @GetMapping("/article/page")
    public R<PageResult<AcGeneratedArticle>> articlePage(AcGeneratedArticlePageReqVO pageReqVO) {
        return page(generatedArticleService.selectArticlePage(pageReqVO));
    }
}
