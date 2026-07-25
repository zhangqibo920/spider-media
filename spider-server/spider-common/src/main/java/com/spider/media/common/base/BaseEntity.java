package com.spider.media.common.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 *
 * <p>所有业务实体的公共父类，提供通用的审计字段和逻辑删除支持。
 * 通过 Jackson 的 @JsonFormat 注解确保时间字段在序列化时使用统一的格式。</p>
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 创建者，记录该记录由哪个用户创建 */
    private String createBy;

    /** 创建时间，自动填充记录创建的时间点 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新者，记录该记录最后由哪个用户修改 */
    private String updateBy;

    /** 更新时间，自动填充记录最后修改的时间点 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 备注，可选的附加说明信息 */
    private String remark;

    /** 逻辑删除标志：'0'=正常存在，'2'=已删除（不物理删除数据，保留历史记录） */
    private String delFlag;
}
