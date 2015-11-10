package com.dtdream;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.crawler.authentication.AuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;

/**
 * Created by lenovo on 2015/8/6.
 */
public class TestCrawler extends WebCrawler{
    BufferedWriter bw = null;
    public TestCrawler() {
        try {
            bw  = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream("C:\\Users\\lenovo\\Desktop\\test\\out")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void visit(Page page) {
        System.out.println(page.getWebURL().getDepth()+","+page.getWebURL().getParentUrl()+","+page.getWebURL().getURL());
        WebURL url = page.getWebURL();
        System.out.println(url.getURL());
        try {
            Document doc = Jsoup.connect(url.getURL()).userAgent("Mozilla").timeout(0).get();
            if(page.getWebURL().getDepth()>=1){
                Elements es = doc.getElementsByTag("tr");
                for(int i=0;i<es.size();i++){
                    bw.write(es.get(i).text());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldVisit(Page page, WebURL url) {
        return true;
    }

    public static void main(String[] args) throws Exception {
        TestCrawler tc = new TestCrawler();
        System.out.println("tc³õÊ¼»¯");
        CrawlConfig config = new  CrawlConfig();
        config.setCrawlStorageFolder("stats");

        PageFetcher fetcher = new PageFetcher(config);
        RobotstxtConfig rc = new RobotstxtConfig();
        RobotstxtServer rs = new RobotstxtServer(rc,fetcher);
        CrawlController controller = new CrawlController(config,fetcher,rs);
        controller.addSeed("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2013/");

        controller.start(TestCrawler.class,10);
    }
}
