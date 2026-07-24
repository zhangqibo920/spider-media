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

@Service
public class AcGeneratedArticleServiceImpl implements IAcGeneratedArticleService {

    private static final Logger log = LoggerFactory.getLogger(AcGeneratedArticleServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AcGeneratedArticleMapper generatedArticleMapper;
    private final AcHotTopicMapper hotTopicMapper;
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
            .build();

    @Value("${ai.models.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${ai.models.deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;

    @Value("${ai.models.zhipu.api-key:}")
    private String zhipuApiKey;

    @Value("${ai.models.zhipu.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String zhipuBaseUrl;

    public AcGeneratedArticleServiceImpl(AcGeneratedArticleMapper generatedArticleMapper,
                                          AcHotTopicMapper hotTopicMapper) {
        this.generatedArticleMapper = generatedArticleMapper;
        this.hotTopicMapper = hotTopicMapper;
    }

    @Override
    public AcGeneratedArticle generateArticle(Long hotTopicId, Long userId, String model) {
        AcHotTopic hotTopic = hotTopicMapper.selectById(hotTopicId);
        if (hotTopic == null) {
            throw new ServiceException(ErrorCodeEnums.AC_HOT_TOPIC_NOT_FOUND);
        }

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

    private String callLLM(String prompt, String model) {
        if ("zhipu".equalsIgnoreCase(model)) {
            return callZhipu(prompt);
        }
        return callDeepseek(prompt);
    }

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
