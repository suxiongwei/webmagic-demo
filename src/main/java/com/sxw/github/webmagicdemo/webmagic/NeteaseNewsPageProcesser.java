package com.sxw.github.webmagicdemo.webmagic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Component
public class NeteaseNewsPageProcesser implements PageProcessor {
    private Site site = Site.me().setDomain("news.163.com")
            .setRetryTimes(3)
            .setSleepTime(1000)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");

    public static final String URL_LIST = "http://news\\.163\\.com/special/\\w+/\\w+\\.html";

    public static final String URL_POST = "http://news\\.163\\.com/.+\\.html";

    public Set<Request> targetRequests = new HashSet<>();

    @Override
    public void process(Page page) {
        //列表页
        if (page.getUrl().regex(URL_LIST).match() || page.getUrl().regex("http://news\\.163\\.com/domestic").match()
                ||page.getUrl().regex("http://news\\.163\\.com/shehui").match()) {
            page.addTargetRequests(page.getHtml().links().regex(URL_POST).all());
            page.addTargetRequests(page.getHtml().links().regex(URL_LIST).all());
            targetRequests.addAll(new HashSet<>(page.getTargetRequests()));
        }else{
            //photoview 新闻和普通列表格式的新闻页面元素不一样
            String url = page.getUrl().get();
            if(url.contains("photoview")){
                page.putField("title", Utils.replaceHTML(page.getHtml().xpath("//div[@class='headline']").toString()));
                page.putField("content", Utils.replaceHTML(page.getHtml().xpath("//div[@class='overview']").toString()));
                page.putField("url", page.getUrl().get());
            }else{
                page.putField("title", Utils.replaceHTML(page.getHtml().xpath("//div[@id='epContentLeft']//h1").toString()));
                page.putField("content", Utils.replaceHTML(page.getHtml().xpath("//div[@id='endText']").toString()));
                page.putField("url", page.getUrl().get());
            }

            String title = page.getResultItems().get("title");
            String content = page.getResultItems().get("content");

            log.info("title="+title);
            log.info("content="+content);
            log.info("url="+url);
            log.info("--------------------------------------------------");
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        String url = "http://news.163.com/domestic/ssss.html";
        log.info("match:[{}]", Pattern.matches(URL_POST, url));
    }
}
