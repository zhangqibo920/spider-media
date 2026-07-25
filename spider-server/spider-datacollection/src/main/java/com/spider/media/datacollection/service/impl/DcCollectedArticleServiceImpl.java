package com.spider.media.datacollection.service.impl;

import com.spider.media.common.mybatis.PageUtils;
import com.spider.media.common.pojo.PageResult;
import com.spider.media.datacollection.controller.vo.DcCollectedArticlePageReqVO;
import com.spider.media.datacollection.entity.DcCollectedArticle;
import com.spider.media.datacollection.entity.DcTargetAccount;
import com.spider.media.datacollection.mapper.DcCollectedArticleMapper;
import com.spider.media.datacollection.mapper.DcTargetAccountMapper;
import com.spider.media.datacollection.service.IDcCollectedArticleService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 采集文章业务层实现类
 *
 * <p>实现对标账号文章的异步采集功能。通过 HTTP 请求获取对标账号主页 HTML，
 * 使用 Jsoup 解析页面提取文章链接，再逐个抓取文章正文内容并保存到数据库。</p>
 *
 * <p>采集策略：
 * <ul>
 *   <li>通过 URL 去重避免重复采集</li>
 *   <li>每个对标账号最多采集 20 篇文章</li>
 *   <li>使用 Jsoup CSS 选择器提取正文段落</li>
 *   <li>自动截取前 200 字作为摘要</li>
 * </ul></p>
 */
@Service
public class DcCollectedArticleServiceImpl implements IDcCollectedArticleService {

    private static final Logger log = LoggerFactory.getLogger(DcCollectedArticleServiceImpl.class);

    /** 采集文章数据访问对象 */
    private final DcCollectedArticleMapper collectedArticleMapper;
    /** 对标账号数据访问对象 */
    private final DcTargetAccountMapper targetAccountMapper;
    /** HTTP 客户端（用于抓取网页内容） */
    private final WebClient webClient = WebClient.builder()
            .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();

    public DcCollectedArticleServiceImpl(DcCollectedArticleMapper collectedArticleMapper,
                                          DcTargetAccountMapper targetAccountMapper) {
        this.collectedArticleMapper = collectedArticleMapper;
        this.targetAccountMapper = targetAccountMapper;
    }

    @Override
    public PageResult<DcCollectedArticle> selectArticlePage(DcCollectedArticlePageReqVO pageReqVO) {
        return PageUtils.selectPage(pageReqVO, () ->
                collectedArticleMapper.selectPage(
                        pageReqVO.getTargetAccountId(),
                        pageReqVO.getPlatform(),
                        pageReqVO.getTitle()
                )
        );
    }

    /**
     * 异步采集对标账号的文章
     *
     * <p>采集流程：
     * <ol>
     *   <li>根据 targetAccountId 查询对标账号信息</li>
     *   <li>获取对标账号的主页 URL</li>
     *   <li>抓取主页 HTML 并解析文章链接列表</li>
     *   <li>逐个抓取文章详情页，提取正文内容</li>
     *   <li>通过 URL 去重后保存到数据库</li>
     * </ol></p>
     *
     * @param targetAccountId 对标账号ID
     */
    @Override
    @Async
    public void collectArticles(Long targetAccountId) {
        log.info("开始采集对标账号数据, targetAccountId={}", targetAccountId);
        try {
            List<DcTargetAccount> accounts = targetAccountMapper.selectList(null, null, null);
            DcTargetAccount target = accounts.stream()
                    .filter(a -> a.getId().equals(targetAccountId))
                    .findFirst().orElse(null);
            if (target == null) {
                log.warn("对标账号不存在, targetAccountId={}", targetAccountId);
                return;
            }

            String url = target.getAccountUrl();
            if (url == null || url.isEmpty()) {
                log.warn("对标账号链接为空, targetAccountId={}", targetAccountId);
                return;
            }

            // 抓取主页 HTML
            String html = webClient.get().uri(url)
                    .retrieve().bodyToMono(String.class).block();

            if (html == null || html.isEmpty()) {
                log.warn("获取页面内容为空, url={}", url);
                return;
            }

            // 解析文章列表并逐个采集
            Document doc = Jsoup.parse(html, url);
            List<DcCollectedArticle> articles = parseArticles(doc, target);
            int saved = 0;
            for (DcCollectedArticle article : articles) {
                DcCollectedArticle exists = collectedArticleMapper.selectByUrl(article.getUrl());
                if (exists == null) {
                    collectedArticleMapper.insert(article);
                    saved++;
                }
            }
            log.info("采集完成, targetAccountId={}, 新增{}篇文章", targetAccountId, saved);
        } catch (Exception e) {
            log.error("采集失败, targetAccountId={}", targetAccountId, e);
        }
    }

    /**
     * 解析 HTML 页面中的文章链接并逐个抓取详情
     *
     * @param doc    Jsoup 解析后的主页文档
     * @param target 对标账号实体
     * @return 采集到的文章列表（最多20篇）
     */
    private List<DcCollectedArticle> parseArticles(Document doc, DcTargetAccount target) {
        List<DcCollectedArticle> articles = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        Long userId = target.getUserId();

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String href = link.absUrl("href");
            String title = link.text().trim();
            // 过滤无效链接：空标题、空链接、指向主页的链接、标题长度异常
            if (title.isEmpty() || href.isEmpty() || href.equals(target.getAccountUrl())) {
                continue;
            }
            if (title.length() < 5 || title.length() > 200) {
                continue;
            }
            if (!href.startsWith("http")) {
                continue;
            }

            // 构建文章实体
            DcCollectedArticle article = new DcCollectedArticle();
            article.setUserId(userId);
            article.setTargetAccountId(target.getId());
            article.setPlatform(target.getPlatform());
            article.setTitle(title);
            article.setUrl(href);
            article.setAuthor(target.getAccountName());
            article.setViewCount(0);
            article.setLikeCount(0);
            article.setCommentCount(0);
            article.setShareCount(0);
            article.setCollectedTime(now);
            article.setCreateBy(target.getAccountName());
            article.setCreateTime(now);

            // 尝试抓取文章详情页的正文内容
            try {
                String articleHtml = webClient.get().uri(href)
                        .retrieve().bodyToMono(String.class).block();
                if (articleHtml != null) {
                    Document articleDoc = Jsoup.parse(articleHtml, href);
                    Elements paragraphs = articleDoc.select("article p, .content p, .article-content p, .post-content p, main p");
                    StringBuilder content = new StringBuilder();
                    for (Element p : paragraphs) {
                        String text = p.text().trim();
                        if (!text.isEmpty()) {
                            content.append(text).append("\n\n");
                        }
                    }
                    if (content.length() > 0) {
                        String fullContent = content.toString().trim();
                        article.setContent(fullContent);
                        article.setSummary(fullContent.length() > 200 ? fullContent.substring(0, 200) + "..." : fullContent);
                    }
                }
            } catch (Exception e) {
                log.debug("获取文章内容失败, url={}", href);
            }

            articles.add(article);
            if (articles.size() >= 20) break;
        }
        return articles;
    }
}
