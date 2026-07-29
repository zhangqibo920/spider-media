package com.spider.media.contentpublish.publisher;

import com.spider.media.contentpublish.entity.PbPlatformAccount;
import com.spider.media.contentpublish.entity.PbPublishTask;

public interface PlatformPublisher {

    String platform();

    PublishResult publish(PbPublishTask task, PbPlatformAccount account);

    record PublishResult(boolean success, String message) {}
}
