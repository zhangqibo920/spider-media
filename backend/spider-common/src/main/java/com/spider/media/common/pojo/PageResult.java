package com.spider.media.common.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果封装
 *
 * <p>统一封装分页查询的返回数据，包含数据列表和总记录数，用于前端分页组件展示。
 * 提供 {@link #empty()} 方法快速构建空分页结果。</p>
 *
 * @param <T> 分页数据中每条记录的泛型类型
 */
@Data
public final class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页的数据列表 */
    private List<T> list;

    /** 符合查询条件的总记录数（用于前端计算总页数） */
    private Long total;

    /** 无参构造 */
    public PageResult() {
    }

    /**
     * 构造分页结果
     *
     * @param list  当前页的数据列表
     * @param total 符合条件的总记录数
     */
    public PageResult(List<T> list, Long total) {
        this.list = list;
        this.total = total;
    }

    /**
     * 快速构建空的分页结果（用于无数据时的返回）
     *
     * @return 空的分页结果对象
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0L);
    }
}
