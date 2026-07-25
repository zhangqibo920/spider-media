package com.spider.media.common.controller;

import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.R;

import java.util.List;

/**
 * Controller 基类
 *
 * <p>提供统一响应构建的快捷方法，所有业务 Controller 继承此类以减少重复代码。
 * 通过 ok()、fail()、page()、list() 等方法快速构建标准格式的响应对象。</p>
 */
public abstract class BaseController {

    /**
     * 构建成功响应（无数据）
     *
     * @return 成功的统一响应对象
     */
    protected <T> R<T> ok() {
        return R.ok();
    }

    /**
     * 构建成功响应（携带数据）
     *
     * @param data 响应数据
     * @return 成功的统一响应对象
     */
    protected <T> R<T> ok(T data) {
        return R.ok(data);
    }

    /**
     * 构建成功响应（携带数据和自定义提示信息）
     *
     * @param data    响应数据
     * @param message 自定义提示信息
     * @return 成功的统一响应对象
     */
    protected <T> R<T> ok(T data, String message) {
        return R.ok(data, message);
    }

    /**
     * 构建失败响应（使用默认错误码）
     *
     * @return 失败的统一响应对象
     */
    protected <T> R<T> fail() {
        return R.fail();
    }

    /**
     * 构建失败响应（自定义错误信息）
     *
     * @param message 错误信息
     * @return 失败的统一响应对象
     */
    protected <T> R<T> fail(String message) {
        return R.fail(message);
    }

    /**
     * 构建失败响应（自定义错误码和错误信息）
     *
     * @param code    错误码
     * @param message 错误信息
     * @return 失败的统一响应对象
     */
    protected <T> R<T> fail(int code, String message) {
        return R.fail(code, message);
    }

    /**
     * 构建分页查询成功响应
     *
     * @param pageResult 分页查询结果
     * @return 包含分页数据的成功响应对象
     */
    protected <T> R<PageResult<T>> page(PageResult<T> pageResult) {
        return R.ok(pageResult);
    }

    /**
     * 构建列表查询成功响应
     *
     * @param list 列表数据
     * @return 包含列表数据的成功响应对象
     */
    protected <T> R<List<T>> list(List<T> list) {
        return R.ok(list);
    }
}
