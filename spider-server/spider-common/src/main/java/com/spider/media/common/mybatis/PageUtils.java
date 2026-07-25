package com.spider.media.common.mybatis;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spider.media.common.pojo.PageParam;
import com.spider.media.common.pojo.PageResult;

import java.util.List;

/**
 * 分页工具类（基于 PageHelper）
 *
 * <p>封装 PageHelper 的分页操作，提供简洁的分页查询入口。
 * 使用方式：在 MyBatis 查询之前调用 {@link #startPage}，PageHelper 会自动拦截下一条 SQL 进行分页处理。</p>
 */
public class PageUtils {

    /**
     * 开始分页（在 MyBatis 查询方法之前调用）
     *
     * <p>PageHelper 使用 ThreadLocal 机制，将分页参数绑定到当前线程，
     * 下一次 MyBatis 查询会自动添加 LIMIT 子句。</p>
     *
     * @param pageParam 分页请求参数（页码和每页条数）
     */
    public static void startPage(PageParam pageParam) {
        PageHelper.startPage(pageParam.getPageNo(), pageParam.getPageSize());
    }

    /**
     * 将 PageHelper 查询结果转换为统一的分页结果
     *
     * <p>PageHelper 执行查询后，返回的 List 会被包装为 PageInfo，
     * 从中提取总记录数构建 {@link PageResult}。</p>
     *
     * @param list PageHelper 分页查询后返回的列表（实际类型为 Page）
     * @return 统一的分页结果
     */
    public static <T> PageResult<T> buildPageResult(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        return new PageResult<>(list, pageInfo.getTotal());
    }

    /**
     * 分页查询的便捷方法（一步完成分页设置 + 查询 + 结果封装）
     *
     * @param pageParam 分页请求参数
     * @param query     分页查询函数（通过函数式接口传入具体的查询逻辑）
     * @return 统一的分页结果
     */
    public static <T> PageResult<T> selectPage(PageParam pageParam, PageQuery<T> query) {
        startPage(pageParam);
        List<T> list = query.execute();
        return buildPageResult(list);
    }

    /**
     * 分页查询函数式接口
     *
     * <p>用于在 {@link #selectPage} 中传入具体的查询逻辑，避免重复编写分页代码。</p>
     */
    @FunctionalInterface
    public interface PageQuery<T> {
        /** 执行查询并返回结果列表 */
        List<T> execute();
    }
}
