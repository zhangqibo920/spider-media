package com.spider.media.hotmonitor.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HmNotification extends BaseEntity {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private String isRead;
    private Long hotTopicId;
}
