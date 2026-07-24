package com.spider.media.common.controller;

import com.spider.media.common.pojo.PageResult;
import com.spider.media.common.result.R;

import java.util.List;

/**
 * Controller 基类，提供统一响应的快捷方法
 */
public abstract class BaseController {

    protected <T> R<T> ok() {
        return R.ok();
    }

    protected <T> R<T> ok(T data) {
        return R.ok(data);
    }

    protected <T> R<T> ok(T data, String message) {
        return R.ok(data, message);
    }

    protected <T> R<T> fail() {
        return R.fail();
    }

    protected <T> R<T> fail(String message) {
        return R.fail(message);
    }

    protected <T> R<T> fail(int code, String message) {
        return R.fail(code, message);
    }

    protected <T> R<PageResult<T>> page(PageResult<T> pageResult) {
        return R.ok(pageResult);
    }

    protected <T> R<List<T>> list(List<T> list) {
        return R.ok(list);
    }
}
