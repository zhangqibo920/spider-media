package com.spider.media.contentpublish.entity;

import com.spider.media.common.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 平台账号实体类
 *
 * <p>对应数据库表 pb_platform_account，用于存储用户在各自媒体平台（如抖音、快手、小红书等）的授权账号信息。
 * 继承 BaseEntity 获得通用字段（创建人、创建时间、更新人、更新时间、备注、删除标记等）。
 * 采用逻辑删除策略，通过 del_flag 字段标记记录是否已删除（'0'=正常，'2'=已删除）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PbPlatformAccount extends BaseEntity {

    /** 账号主键ID，自增 */
    private Long id;

    /** 关联的用户ID，表示该账号属于哪个系统用户 */
    private Long userId;

    /** 平台类型标识，如 "douyin"、"kuaishou"、"xiaohongshu" 等 */
    @NotBlank(message = "平台类型不能为空")
    @Size(max = 50, message = "平台类型长度不能超过50")
    private String platform;

    /** 平台账号的显示名称（如抖音昵称） */
    @NotBlank(message = "账号名称不能为空")
    @Size(max = 100, message = "账号名称长度不能超过100")
    private String accountName;

    /** 平台账号的唯一ID（如抖音的 open_id / union_id） */
    private String accountId;

    /** 平台授权的 Access Token，用于调用平台 API */
    private String accessToken;

    /** 平台授权的 Refresh Token，用于在 Access Token 过期后刷新 */
    private String refreshToken;

    /** Access Token 的过期时间，过期后需使用 Refresh Token 刷新 */
    private LocalDateTime tokenExpireTime;

    /** 账号状态：'0'=正常可用，'1'=停用 */
    private String status;

    /** 账号分组名称，用于按业务维度对账号进行分类管理 */
    private String groupName;
}
