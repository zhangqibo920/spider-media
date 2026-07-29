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
 * AI 创作控制器
 *
 * <p>提供热点话题抓取、AI 文章生成、文章查询等接口。
 * 所有接口路径在 /api/ai 下，需要用户登录后访问。</p>
 */
@RestController
@RequestMapping("/api/ai")
public class AcCreationController extends BaseController {

    /** 热点话题业务层服务 */
    private final IAcHotTopicService hotTopicService;
    /** AI 文章生成业务层服务 */
    private final IAcGeneratedArticleService generatedArticleService;

    public AcCreationController(IAcHotTopicService hotTopicService,
                                 IAcGeneratedArticleService generatedArticleService) {
        this.hotTopicService = hotTopicService;
        this.generatedArticleService = generatedArticleService;
    }

    /**
     * 抓取热点话题（异步执行）
     *
     * <p>从指定平台抓取热搜榜单，保存到当前用户的热点列表中。</p>
     *
     * @param platform 平台类型（weibo/douyin/zhihu/toutiao）
     * @return 操作结果
     */
    @PostMapping("/hotTopic/fetch")
    public R<Void> fetchHotTopics(@RequestParam(value = "platform") String platform) {
        Long userId = LoginUser.getUserId();
        hotTopicService.fetchHotTopics(platform, userId);
        return ok();
    }

    /**
     * 查询当前用户的热点话题列表
     *
     * @return 热点话题列表
     */
    @GetMapping("/hotTopic/list")
    public R<List<AcHotTopic>> listHotTopics() {
        Long userId = LoginUser.getUserId();
        return list(hotTopicService.selectHotTopicList(userId));
    }

    /**
     * 根据热点话题生成 AI 文章
     *
     * <p>根据指定的热点话题，调用 AI 模型生成一篇自媒体文章。</p>
     *
     * @param request 包含 hotTopicId 和可选 model 的 JSON 请求体
     * @return 生成的文章实体
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
     * 分页查询 AI 生成文章列表
     *
     * @param pageReqVO 分页查询参数
     * @return 文章分页结果
     */
    @GetMapping("/article/page")
    public R<PageResult<AcGeneratedArticle>> articlePage(AcGeneratedArticlePageReqVO pageReqVO) {
        pageReqVO.setUserId(LoginUser.getUserId());
        return page(generatedArticleService.selectArticlePage(pageReqVO));
    }

    /**
     * 删除热点话题
     *
     * @param id 热点话题ID
     * @return 操作结果
     */
    @DeleteMapping("/hotTopic/{id}")
    public R<Void> deleteHotTopic(@PathVariable Long id) {
        hotTopicService.deleteHotTopic(id);
        return ok();
    }

    /**
     * 删除 AI 生成文章
     *
     * @param id 文章ID
     * @return 操作结果
     */
    @DeleteMapping("/article/{id}")
    public R<Void> deleteGeneratedArticle(@PathVariable Long id) {
        generatedArticleService.deleteGeneratedArticle(id);
        return ok();
    }
}
