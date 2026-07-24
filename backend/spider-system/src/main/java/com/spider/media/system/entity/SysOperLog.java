package com.spider.media.system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysOperLog implements Serializable {

    private Long id;
    private String username;
    private String module;
    private String action;
    private String description;
    private String ip;
    private String method;
    private String params;
    private Integer status;
    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
