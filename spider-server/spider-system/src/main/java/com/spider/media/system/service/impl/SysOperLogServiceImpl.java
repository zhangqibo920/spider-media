package com.spider.media.system.service.impl;

import com.spider.media.system.entity.SysOperLog;
import com.spider.media.system.mapper.SysOperLogMapper;
import com.spider.media.system.service.ISysOperLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志业务层实现类
 *
 * <p>实现操作日志的记录和查询操作。记录日志时捕获异常避免影响主业务流程。</p>
 */
@Service
public class SysOperLogServiceImpl implements ISysOperLogService {

    private static final Logger log = LoggerFactory.getLogger(SysOperLogServiceImpl.class);

    /** 操作日志数据访问对象 */
    private final SysOperLogMapper operLogMapper;

    public SysOperLogServiceImpl(SysOperLogMapper operLogMapper) {
        this.operLogMapper = operLogMapper;
    }

    /**
     * 记录操作日志（失败时仅打印警告，不影响主业务流程）
     */
    @Override
    public void recordLog(SysOperLog operLog) {
        try {
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            log.warn("记录操作日志失败: {}", e.getMessage());
        }
    }

    @Override
    public List<SysOperLog> selectLogPage(String username, String module, int pageNo, int pageSize) {
        return operLogMapper.selectPage(username, module, pageNo, pageSize);
    }

    @Override
    public long selectLogCount(String username, String module) {
        return operLogMapper.selectCount(username, module);
    }
}
