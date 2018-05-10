package site.zido.elise;

import site.zido.elise.configurable.*;
import site.zido.elise.downloader.HttpClientDownloader;
import site.zido.elise.pipeline.MappedPageModelPipeline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import site.zido.elise.pipeline.ModelPipeline;
import site.zido.elise.processor.ExtractorPageProcessor;

import java.util.List;
import java.util.Map;

/**
 * 分布式爬虫测试
 *
 * @author zido
 * @date 2018/04/16
 */
public class ConfigurableSpiderTest {
    private Task task;

    @Before
    public void init() {
        DefRootExtractor def = new DefRootExtractor();
        def.setName("github");
        def.addHelpUrl(new ConfigurableUrlFinder().setValue("https://github\\.com/\\w+\\?tab=repositories"),
                new ConfigurableUrlFinder().setValue("https://github\\.com/\\w+"),
                new ConfigurableUrlFinder().setValue("https://github\\.com/explore/*"));
        def.addTargetUrl(new ConfigurableUrlFinder().setValue("https://github\\.com/\\w+/\\w+"));

        def.addChildren(new DefExtractor().setName("name")
                .setType(ExpressionType.XPATH)
                .setValue("//h1[@class='public']/strong/a/text()")
                .setNullable(false));
        def.addChildren(new DefExtractor().setName("author")
                .setType(ExpressionType.REGEX)
                .setSource(Extractor.Source.URL)
                .setValue("https://github\\.com/(\\w+)/.*")
                .setNullable(false));
        def.addChildren(new DefExtractor().setName("readme")
                .setType(ExpressionType.XPATH)
                .setValue("//div[@id='readme']/tidyText()")
                .setNullable(false));
        Site site = new Site().setRetryTimes(3).setSleepTime(0);
        task = new DistributedTask(123L, site, def);
    }

    @Test
    public void testProcessor() {
        HttpClientDownloader downloader = new HttpClientDownloader();
        Page page = downloader.download(new Request("https://github.com/zidoshare/bone"), task);
        ExtractorPageProcessor processor = new ExtractorPageProcessor();
        ResultItem resultItem = processor.process(task, page, (task, request) -> System.out.println(request.getUrl()));
        Object github = resultItem.get("github");
        if (github instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) github;
            Assert.assertEquals("bone", map.get("name"));
            Assert.assertEquals("zidoshare", map.get("author"));
        }
    }

    @Test
    public void testModelPipeline() {
        HttpClientDownloader downloader = new HttpClientDownloader();
        Page page = downloader.download(new Request("https://github.com/zidoshare/bone"), task);
        ExtractorPageProcessor processor = new ExtractorPageProcessor();
        ResultItem resultItem = processor.process(task, page, (task, request) -> System.out.println(request.getUrl()));
        MappedPageModelPipeline mappedPipeline = new MappedPageModelPipeline();
        ModelPipeline pipeline = new ModelPipeline();
        pipeline.putPageModelPipeline("github", mappedPipeline);
        pipeline.process(resultItem, task);
        List<Map<String, Object>> collected = mappedPipeline.getCollected();
        Assert.assertEquals(1, collected.size());
    }
}