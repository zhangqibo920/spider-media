package com.spider.media.contentpublish.publisher;

import com.spider.media.contentpublish.entity.PbPlatformAccount;
import com.spider.media.contentpublish.entity.PbPublishTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimulatedPublisher implements PlatformPublisher {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPublisher.class);

    @Override
    public String platform() {
        return "*";
    }

    @Override
    public PublishResult publish(PbPublishTask task, PbPlatformAccount account) {
        String platform = task.getPlatform();
        log.info("模拟发布到 {} 平台, 标题={}, 账号={}", platform, task.getTitle(), account.getAccountName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new PublishResult(true, "模拟发布成功");
    }
}
