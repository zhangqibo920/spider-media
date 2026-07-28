package com.spider.media.hotmonitor.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class HmKeyword extends BaseEntity {

    private Long id;
    private Long userId;
    private String keyword;
    private String status;
    private Integer intervalMin;
    private String notifyEmail;
    private String notifySite;
    private LocalDateTime lastFetchTime;
}
