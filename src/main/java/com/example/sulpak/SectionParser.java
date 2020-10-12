package com.example.sulpak;

import com.example.sulpak.model.Category;
import com.example.sulpak.model.Item;
import com.example.sulpak.model.MainGroup;
import com.example.sulpak.model.Section;
import com.example.sulpak.repository.CategoryRepository;
import com.example.sulpak.repository.ItemRepository;
import com.example.sulpak.repository.MainGroupRepository;
import com.example.sulpak.repository.SectionRepository;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SectionParser {
    private static final Logger LOG = LoggerFactory.getLogger(SectionParser.class);

    private static final Set<String> SECTIONS = Set.of("Телефоны и гаджеты", "Теле и аудио техника", "Ноутбуки и компьютеры", "Фото и видео техника",
            "Игры и развлечения", "Техника для дома", "Техника для кухни", "Встраиваемая техника");

    private static final Set<String> GROUPS_EXCEPTIONS = Set.of("Купить дешевле");
    private static final String URL = "https://www.sulpak.kz/";

    private static final long ONE_SECOND_MS = 1000L;
    private static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;
    private static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;
    private static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;
    private static final long ONE_WEEK_MS = 7 * ONE_DAY_MS;


    @Value("${sulpak.api.chunk-size}")
    private Integer chunkSize;
    @Value("${sulpak.thread-pool.pool-size}")
    private Integer threadPoolSize;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MainGroupRepository mainGroupRepository;
    @Autowired
    private ItemRepository itemRepository;



    @Scheduled(fixedDelay = ONE_WEEK_MS)
    @Transactional
    public void getSections() throws IOException {
        Instant start = Instant.now();
        Document newsPage = Jsoup.connect(URL).get();
        LOG.info("Получили главную страницу, ищем секции...");
        Elements sectionElements = newsPage.select(".catalog-category-item a");
        for (Element sectionElement : sectionElements) {
            String text = sectionElement.text();
            if (SECTIONS.contains(text)) {
                LOG.info("Получаем {}...", text);
                String sectionUrl = sectionElement.absUrl("href");
                Section section = sectionRepository.findOneByUrl(sectionUrl)
                        .orElseGet(() -> sectionRepository.save(new Section(text, sectionUrl)));
                Document groupPage = Jsoup.connect(sectionUrl).get();
                LOG.info("Получили {}, ищем группы...", text);
                Elements groupElements = groupPage.select(".portal-menu-title a");
                for (Element groupElement : groupElements) {
                    String groupUrl = groupElement.absUrl("href");
                    String groupText = groupElement.text();
                    LOG.info("Группа  {}", groupText);
                    if (!GROUPS_EXCEPTIONS.contains(groupText)) {
                        MainGroup group = mainGroupRepository.findOneByUrl(sectionUrl)
                                .orElseGet(() -> mainGroupRepository.save(new MainGroup(groupText, groupUrl, section)));
                        Document categoryPage = Jsoup.connect(groupUrl).get();
                       // Elements categoryElements = groupElement.select(".portal-menu-items a");
                        Elements categoryElements = categoryPage.select(".portal-parts-list a");
                        for (Element categoryElement : categoryElements) {
                            String categoryLink = categoryElement.absUrl("href");
                            String categoryText = categoryElement.text();
                            LOG.info("\tКатегория  {}", categoryText);
                            if (!categoryRepository.existsByUrl(sectionUrl)) {
                                categoryRepository.save(new Category(categoryText, categoryLink, group));
                            }
                        }
                    }
                }
            }
        }
    }

    @Scheduled(initialDelay = 1200, fixedDelay = ONE_WEEK_MS)
    @Transactional
    public void getAdditionalArticleInfo() throws InterruptedException {
        LOG.info("Получаем дополнитульную информацию о товарe...");
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        int page = 0;
        List<Category> categories;

        // 1. offset + limit
        // 2. page + pageSize
        //   offset = page * pageSize;  limit = pageSize;
        while (!(categories = categoryRepository.getChunk(PageRequest.of(page++, chunkSize))).isEmpty()) {
            LOG.info("Получили из базы {} категорий", categories.size());
            CountDownLatch latch = new CountDownLatch(categories.size());
            for (Category category : categories) {
                executorService.execute(new ItemsUpdateTask(itemRepository,category,latch));
            }
            LOG.info("Задачи запущены, ожидаем завершения выполнения...");
            latch.await();
            LOG.info("Задачи выполнены, следующая порция...");
        }
        executorService.shutdown();
    }
}
