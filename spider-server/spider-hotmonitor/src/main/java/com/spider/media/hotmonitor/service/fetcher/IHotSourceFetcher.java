package com.spider.media.hotmonitor.service.fetcher;

import com.spider.media.aicreation.entity.AcHotTopic;

import java.util.List;

public interface IHotSourceFetcher {

    String sourceName();

    List<AcHotTopic> fetch(String keyword);
}
