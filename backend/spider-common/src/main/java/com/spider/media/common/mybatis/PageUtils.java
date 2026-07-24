package com.spider.media.common.mybatis;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spider.media.common.pojo.PageParam;
import com.spider.media.common.pojo.PageResult;

import java.util.List;

/**
 * 分页工具类（基于 PageHelper）
 */
public class PageUtils {

    /**
     * 开始分页（在查询之前调用）
     */
    public static void startPage(PageParam pageParam) {
        PageHelper.startPage(pageParam.getPageNo(), pageParam.getPageSize());
    }

    /**
     * 将查询结果转换为 PageResult
     */
    public static <T> PageResult<T> buildPageResult(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        return new PageResult<>(list, pageInfo.getTotal());
    }

    /**
     * 分页查询的便捷方法
     */
    public static <T> PageResult<T> selectPage(PageParam pageParam, PageQuery<T> query) {
        startPage(pageParam);
        List<T> list = query.execute();
        return buildPageResult(list);
    }

    /**
     * 分页查询函数式接口
     */
    @FunctionalInterface
    public interface PageQuery<T> {
        List<T> execute();
    }
}
