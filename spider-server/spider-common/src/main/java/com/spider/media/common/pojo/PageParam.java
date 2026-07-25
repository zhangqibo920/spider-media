package com.spider.media.common.pojo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求参数基类
 *
 * <p>所有分页查询请求 VO 的父类，统一页码和每页条数的参数定义与校验规则。
 * 使用 Jakarta Validation 注解确保参数合法性，防止非法分页参数导致性能问题。</p>
 */
@Data
public class PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 默认页码 */
    private static final Integer PAGE_NO = 1;
    /** 默认每页条数 */
    private static final Integer PAGE_SIZE = 10;

    /** 页码，从 1 开始计数 */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为 1")
    private Integer pageNo = PAGE_NO;

    /** 每页条数，最大限制为 100，防止一次查询过多数据 */
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小值为 1")
    @Max(value = 100, message = "每页条数最大值为 100")
    private Integer pageSize = PAGE_SIZE;
}
