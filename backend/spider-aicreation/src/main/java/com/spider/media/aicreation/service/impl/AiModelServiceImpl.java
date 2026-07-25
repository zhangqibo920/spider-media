package com.spider.media.aicreation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.media.aicreation.entity.AiModel;
import com.spider.media.aicreation.mapper.AiModelMapper;
import com.spider.media.aicreation.service.IAiModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 模型业务层实现类
 *
 * <p>管理 AI 模型的增删改查、启停控制和连通性测试。</p>
 */
@Service
public class AiModelServiceImpl implements IAiModelService {

    private static final Logger log = LoggerFactory.getLogger(AiModelServiceImpl.class);

    private final AiModelMapper modelMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
            .build();

    public AiModelServiceImpl(AiModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public List<AiModel> selectModelList() {
        return modelMapper.selectAll();
    }

    @Override
    public AiModel selectModelById(Long id) {
        return modelMapper.selectById(id);
    }

    @Override
    public AiModel selectEnabledModel(String modelKey) {
        return modelMapper.selectByModelKey(modelKey);
    }

    @Override
    public int insertModel(AiModel model) {
        if (model.getEnabled() == null) model.setEnabled("N");
        if (model.getTestStatus() == null) model.setTestStatus("UNTESTED");
        if (model.getSortOrder() == null) model.setSortOrder(0);
        model.setCreateTime(LocalDateTime.now());
        return modelMapper.insert(model);
    }

    @Override
    public int updateModel(AiModel model) {
        model.setUpdateTime(LocalDateTime.now());
        return modelMapper.update(model);
    }

    @Override
    public int deleteModel(Long id) {
        return modelMapper.deleteById(id);
    }

    @Override
    public int toggleModel(Long id, String enabled) {
        AiModel model = modelMapper.selectById(id);
        if (model == null) return 0;
        model.setEnabled(enabled);
        model.setUpdateTime(LocalDateTime.now());
        return modelMapper.update(model);
    }

    /**
     * 测试模型连通性
     *
     * <p>发送一个简单的聊天请求到模型 API，验证密钥和地址是否正确。</p>
     *
     * @return 测试结果描述
     */
    @Override
    public String testModel(Long id) {
        AiModel model = modelMapper.selectById(id);
        if (model == null) {
            return "模型不存在";
        }

        // 更新状态为测试中
        modelMapper.updateTestStatus(id, "TESTING", "正在测试...");

        try {
            String apiKey = model.getApiKey();
            String baseUrl = model.getBaseUrl();
            String modelKey = model.getModelKey();

            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("API密钥未配置");
            }
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new RuntimeException("API地址未配置");
            }

            // 构建测试请求
            Map<String, Object> body = Map.of(
                    "model", modelKey,
                    "messages", new Object[]{
                            Map.of("role", "user", "content", "请回复'连接成功'四个字")
                    },
                    "max_tokens", 20
            );

            // 发送请求
            String json = webClient.post()
                    .uri(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 解析响应
            JsonNode root = objectMapper.readTree(json);
            String content = root.path("choices").get(0).path("message").path("content").asText("");
            String finishReason = root.path("choices").get(0).path("finish_reason").asText("");

            if (content.isEmpty()) {
                throw new RuntimeException("API返回内容为空");
            }

            // 测试成功
            String message = "连接成功! 模型回复: " + content;
            modelMapper.updateTestStatus(id, "SUCCESS", message);
            log.info("模型测试成功: {}, 回复: {}", modelKey, content);
            return message;

        } catch (Exception e) {
            String errorMsg = "测试失败: " + e.getMessage();
            modelMapper.updateTestStatus(id, "FAILED", errorMsg);
            log.warn("模型测试失败: {}, 原因: {}", model.getModelKey(), e.getMessage());
            return errorMsg;
        }
    }
}
