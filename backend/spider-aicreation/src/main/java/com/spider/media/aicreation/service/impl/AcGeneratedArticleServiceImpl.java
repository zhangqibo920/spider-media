package com.spider.media.aicreation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.media.aicreation.controller.vo.AcGeneratedArticlePageReqVO;
import com.spider.media.aicreation.entity.AcGeneratedArticle;
import com.spider.media.aicreation.entity.AcHotTopic;
import com.spider.media.aicreation.mapper.AcGeneratedArticleMapper;
import com.spider.media.aicreation.mapper.AcHotTopicMapper;
import com.spider.media.aicreation.service.IAcGeneratedArticleService;
import com.spider.media.common.exception.ServiceException;
import com.spider.media.common.mybatis.PageUtils;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.ErrorCodeEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 生成文章业务层实现类
 *
 * <p>根据热点话题调用 AI 大模型（DeepSeek / 智谱）生成自媒体文章。
 * 支持多模型切换，文章生成后自动保存到数据库。</p>
 *
 * <p>生成流程：
 * <ol>
 *   <li>根据 hotTopicId 查询热点话题信息</li>
 *   <li>构建针对自媒体平台的 Prompt</li>
 *   <li>调用指定 AI 模型的 API 获取生成内容</li>
 *   <li>将结果保存到 ac_generated_article 表</li>
 * </ol></p>
 */
@Service
public class AcGeneratedArticleServiceImpl implements IAcGeneratedArticleService {

    private static final Logger log = LoggerFactory.getLogger(AcGeneratedArticleServiceImpl.class);
    /** JSON 序列化工具 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** AI 生成文章数据访问对象 */
    private final AcGeneratedArticleMapper generatedArticleMapper;
    /** 热点话题数据访问对象 */
    private final AcHotTopicMapper hotTopicMapper;
    /** HTTP 客户端（用于调用 AI API） */
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
            .build();

    /** DeepSeek API 密钥 */
    @Value("${ai.models.deepseek.api-key:}")
    private String deepseekApiKey;

    /** DeepSeek API 基础地址 */
    @Value("${ai.models.deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;

    /** 智谱 API 密钥 */
    @Value("${ai.models.zhipu.api-key:}")
    private String zhipuApiKey;

    /** 智谱 API 基础地址 */
    @Value("${ai.models.zhipu.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String zhipuBaseUrl;

    public AcGeneratedArticleServiceImpl(AcGeneratedArticleMapper generatedArticleMapper,
                                          AcHotTopicMapper hotTopicMapper) {
        this.generatedArticleMapper = generatedArticleMapper;
        this.hotTopicMapper = hotTopicMapper;
    }

    /**
     * 根据热点话题生成 AI 文章
     *
     * @param hotTopicId 热点话题ID
     * @param userId     用户ID
     * @param model      AI 模型标识（"deepseek" 或 "zhipu"）
     * @return 生成的文章实体
     */
    @Override
    public AcGeneratedArticle generateArticle(Long hotTopicId, Long userId, String model) {
        AcHotTopic hotTopic = hotTopicMapper.selectById(hotTopicId);
        if (hotTopic == null) {
            throw new ServiceException(ErrorCodeEnums.AC_HOT_TOPIC_NOT_FOUND);
        }

        // 创建文章记录（状态为 GENERATING）
        AcGeneratedArticle article = new AcGeneratedArticle();
        article.setUserId(userId);
        article.setHotTopicId(hotTopicId);
        article.setModelUsed(model);
        article.setStatus("GENERATING");
        article.setCreateBy(String.valueOf(userId));
        article.setCreateTime(LocalDateTime.now());
        generatedArticleMapper.insert(article);

        try {
            String prompt = buildPrompt(hotTopic);
            String result = callLLM(prompt, model);

            article.setTitle(hotTopic.getTitle());
            article.setContent(result);
            article.setSummary(result.length() > 200 ? result.substring(0, 200) + "..." : result);
            article.setWordCount(result.length());
            article.setStatus("COMPLETED");
        } catch (Exception e) {
            log.error("AI文章生成失败", e);
            article.setTitle(hotTopic.getTitle());
            article.setContent("生成失败: " + e.getMessage());
            article.setStatus("FAILED");
        }

        generatedArticleMapper.update(article);
        return article;
    }

    @Override
    public PageResult<AcGeneratedArticle> selectArticlePage(AcGeneratedArticlePageReqVO pageReqVO) {
        return PageUtils.selectPage(pageReqVO, () ->
                generatedArticleMapper.selectPage(
                        pageReqVO.getUserId(),
                        pageReqVO.getStatus(),
                        pageReqVO.getTitle()
                )
        );
    }

    /**
     * 构建自媒体文章生成的 Prompt
     *
     * @param topic 热点话题
     * @return 完整的 Prompt 字符串
     */
    private String buildPrompt(AcHotTopic topic) {
        return "你是一位专业的自媒体内容创作者。请根据以下热点话题撰写一篇高质量的自媒体文章。\n\n" +
                "话题：" + topic.getTitle() + "\n" +
                (topic.getDescription() != null ? "话题描述：" + topic.getDescription() + "\n" : "") +
                "平台：" + topic.getPlatform() + "\n\n" +
                "要求：\n" +
                "1. 标题吸引眼球，适合自媒体平台\n" +
                "2. 内容原创、有深度、有观点\n" +
                "3. 字数在800-1500字之间\n" +
                "4. 适合在" + topic.getPlatform() + "平台发布\n" +
                "5. 分段清晰，每段不超过200字\n" +
                "6. 结尾有互动引导\n\n" +
                "请直接输出文章内容，不需要额外说明。";
    }

    /**
     * 根据模型标识调用对应的 AI API
     *
     * @param prompt 完整的 Prompt
     * @param model  模型标识
     * @return AI 生成的文本内容
     */
    private String callLLM(String prompt, String model) {
        if ("zhipu".equalsIgnoreCase(model)) {
            return callZhipu(prompt);
        }
        return callDeepseek(prompt);
    }

    /**
     * 调用 DeepSeek API 生成文章
     *
     * @param prompt 完整的 Prompt
     * @return AI 生成的文本内容
     */
    private String callDeepseek(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", "deepseek-chat",
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    },
                    "max_tokens", 2000,
                    "temperature", 0.7
            );

            String json = webClient.post()
                    .uri(deepseekBaseUrl + "/v1/chat/completions")
                    .header("Authorization", "Bearer " + deepseekApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve().bodyToMono(String.class).block();

            JsonNode root = objectMapper.readTree(json);
            return root.path("choices").get(0).path("message").path("content").asText("生成失败");
        } catch (Exception e) {
            log.error("DeepSeek API调用失败", e);
            return "DeepSeek API调用失败: " + e.getMessage();
        }
    }

    /**
     * 调用智谱 API 生成文章
     *
     * @param prompt 完整的 Prompt
     * @return AI 生成的文本内容
     */
    private String callZhipu(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", "glm-4",
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    }
            );

            String json = webClient.post()
                    .uri(zhipuBaseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + zhipuApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve().bodyToMono(String.class).block();

            JsonNode root = objectMapper.readTree(json);
            return root.path("choices").get(0).path("message").path("content").asText("生成失败");
        } catch (Exception e) {
            log.error("智谱 API调用失败", e);
            return "智谱 API调用失败: " + e.getMessage();
        }
    }
}
