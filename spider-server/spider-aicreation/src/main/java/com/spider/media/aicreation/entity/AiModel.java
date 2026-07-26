package com.spider.media.aicreation.entity;

import com.spider.media.common.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI 模型实体类
 *
 * <p>对应数据库表 ai_model，存储系统中所有可用的 AI 模型配置。
 * 管理员可通过后台管理界面配置、测试和启停模型。</p>
 *
 * <p>字段说明：
 * <ul>
 *   <li>model_key: 模型唯一标识（如 "deepseek-chat"、"glm-4"）</li>
 *   <li>model_name: 模型显示名称（如 "DeepSeek Chat"、"智谱 GLM-4"）</li>
 *   <li>provider: 模型提供方（如 "deepseek"、"zhipu"）</li>
 *   <li>api_key: API 密钥</li>
 *   <li>base_url: API 基础地址</li>
 *   <li>enabled: 是否启用</li>
 *   <li>test_status: 最近一次测试状态（SUCCESS/FAILED/UNTESTED）</li>
 *   <li>test_time: 最近一次测试时间</li>
 *   <li>test_message: 测试结果详情</li>
 * </ul></p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiModel extends BaseEntity {

    /** 模型主键ID */
    private Long id;

    /** 模型唯一标识（如 "deepseek-chat"、"glm-4"） */
    @NotBlank(message = "模型标识不能为空")
    @Size(max = 100, message = "模型标识长度不能超过100")
    private String modelKey;

    /** 模型显示名称（如 "DeepSeek Chat"、"智谱 GLM-4"） */
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100")
    private String modelName;

    /** 模型提供方（如 "deepseek"、"zhipu"） */
    @NotBlank(message = "模型提供方不能为空")
    @Size(max = 50, message = "模型提供方长度不能超过50")
    private String provider;

    /** API 密钥 */
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;

    /** API 基础地址 */
    @NotBlank(message = "API地址不能为空")
    private String baseUrl;

    /** 是否启用：'Y'=启用，'N'=禁用 */
    private String enabled;

    /** 排序序号（控制前端显示顺序） */
    private Integer sortOrder;

    /** 最近测试状态：UNTESTED/TESTING/SUCCESS/FAILED */
    private String testStatus;

    /** 最近测试时间 */
    private LocalDateTime testTime;

    /** 测试结果详情（成功或失败信息） */
    private String testMessage;
}
