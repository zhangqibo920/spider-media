package com.spider.media.hotmonitor.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.media.aicreation.entity.AiModel;
import com.spider.media.aicreation.service.IAiModelService;
import com.spider.media.hotmonitor.ai.dto.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Component
public class HmAiAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(HmAiAnalyzer.class);
    private static final String DEFAULT_MODEL_KEY = "deepseek-chat";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IAiModelService aiModelService;
    private final WebClient webClient;

    public HmAiAnalyzer(IAiModelService aiModelService, WebClient.Builder webClientBuilder) {
        this.aiModelService = aiModelService;
        this.webClient = webClientBuilder
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    public AnalysisResult analyze(String keyword, String title, String description) {
        AnalysisResult result = new AnalysisResult();
        result.setRelevance(50);
        result.setImportance(3);
        result.setIsFake("unverified");

        try {
            String prompt = buildPrompt(keyword, title, description);
            String response = callLLM(prompt);
            result = parseResponse(response);
        } catch (Exception e) {
            log.warn("AI分析失败, keyword={}, title={}", keyword, title, e);
        }
        return result;
    }

    private String buildPrompt(String keyword, String title, String description) {
        return "你是一个热点分析助手。请严格分析以下热点话题与监控关键词的相关性。\n\n" +
                "监控关键词：" + keyword + "\n" +
                "热点话题标题：" + title + "\n" +
                (description != null && !description.isEmpty() ? "热点话题描述：" + description + "\n\n" : "\n") +
                "请以JSON格式返回，不要包含其他内容：\n" +
                "{\n" +
                "  \"relevance\": 0-100,\n" +
                "  \"summary\": \"一句话摘要(不超过50字)\",\n" +
                "  \"importance\": 1-5,\n" +
                "  \"isFake\": \"true/false/suspicious/unverified\"\n" +
                "}";
    }

    private AnalysisResult parseResponse(String jsonText) {
        AnalysisResult result = new AnalysisResult();
        result.setRelevance(50);
        result.setImportance(3);
        result.setIsFake("unverified");
        try {
            String json = jsonText.trim();
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
            }
            JsonNode root = objectMapper.readTree(json);
            if (root.has("relevance")) result.setRelevance(Math.max(0, Math.min(100, root.get("relevance").asInt(50))));
            if (root.has("summary")) result.setSummary(root.get("summary").asText(""));
            if (root.has("importance")) result.setImportance(Math.max(1, Math.min(5, root.get("importance").asInt(3))));
            if (root.has("isFake")) result.setIsFake(root.get("isFake").asText("unverified"));
        } catch (Exception e) {
            log.warn("解析AI响应失败: {}", jsonText, e);
        }
        return result;
    }

    private String callLLM(String prompt) {
        AiModel model = aiModelService.selectEnabledModel(DEFAULT_MODEL_KEY);
        if (model == null) {
            log.warn("AI模型未配置或未启用, modelKey={}", DEFAULT_MODEL_KEY);
            return "{}";
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", model.getModelKey(),
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    },
                    "max_tokens", 300,
                    "temperature", 0.3
            );

            String json = webClient.post()
                    .uri(model.getBaseUrl() + "/v1/chat/completions")
                    .header("Authorization", "Bearer " + model.getApiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve().bodyToMono(String.class)
                    .retryWhen(Retry.backoff(1, Duration.ofSeconds(1)))
                    .block();

            JsonNode root = objectMapper.readTree(json);
            return root.path("choices").get(0).path("message").path("content").asText("{}");
        } catch (Exception e) {
            log.error("AI API调用失败", e);
            return "{}";
        }
    }
}
