package com.spider.media.datacollection.entity;

import com.spider.media.common.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对标账号实体类
 *
 * <p>对应数据库表 dc_target_account，存储用户配置的对标账号信息。
 * 对标账号是用户希望采集其内容的自媒体账号，系统会定期抓取对标账号发布的文章。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DcTargetAccount extends BaseEntity {

    /** 账号主键ID */
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 平台类型（如 "weibo"、"douyin"、"zhihu"） */
    @NotBlank(message = "平台类型不能为空")
    @Size(max = 50, message = "平台类型长度不能超过50")
    private String platform;

    /** 对标账号的显示名称 */
    @NotBlank(message = "账号名称不能为空")
    @Size(max = 100, message = "账号名称长度不能超过100")
    private String accountName;

    /** 对标账号的唯一ID */
    private String accountId;

    /** 对标账号的主页链接（用于采集文章） */
    @NotBlank(message = "账号链接不能为空")
    private String accountUrl;

    /** 分组名称（用于按业务维度分类管理对标账号） */
    private String groupName;

    /** 账号状态：'0'=正常，'1'=停用 */
    private String status;

    /** 对标账号的描述/备注信息 */
    private String description;
}
