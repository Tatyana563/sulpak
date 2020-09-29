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



    @Scheduled(fixedRate = 300000)
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
                Document groupPage = Jsoup.connect(sectionUrl).get();//телефоны и гаджеты
                LOG.info("Получили {}, ищем группы...", text);
                Elements groupElements = groupPage.select(".portal-menu-title a");
                for (Element groupElement : groupElements) {
                    String groupUrl = groupElement.absUrl("href");
                    String groupText = groupElement.text();
                    LOG.info("Группа  {}", groupText);
                    if (!GROUPS_EXCEPTIONS.contains(groupText)) {
                        MainGroup group = mainGroupRepository.findOneByUrl(sectionUrl)
                                .orElseGet(() -> mainGroupRepository.save(new MainGroup(groupText, groupUrl, section)));
                        Elements categoryElements = groupElement
                                .closest(".portal-menu-block.category-block")
                                .select(".portal-menu-items a");
                        for (Element categoryElement : categoryElements) {
                            String categoryLink = categoryElement.absUrl("href");
                            String categoryText = categoryElement.text();
                            LOG.info("\tКатегория  {}", categoryText);
                            if (!categoryRepository.existsByUrl(sectionUrl)) {
                                categoryRepository.save(new Category(categoryText, categoryLink,false, group));
                            }
                            Instant end = Instant.now();
                            System.out.println("PROCEESS OF GETTING CATEGORIES FINISHED FOR: "+Duration.between(start, end));
                        }
                    }
                }
            }
        }
    }

    @Scheduled(initialDelay = 120000, fixedRate = 30000000)
    @Transactional
    public void getAdditionalArticleInfo() throws InterruptedException {
        LOG.info("Получаем дополнитульную информацию о товарe...");
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        PageRequest pageRequest = PageRequest.of(0, chunkSize);
        List<Category> chunk = categoryRepository.getChunk(pageRequest);
        LOG.info("Получили из базы {} категорий", chunk.size());
        while (!chunk.isEmpty()) {
            CountDownLatch latch = new CountDownLatch(chunk.size());
            for (Category category : chunk) {
                executorService.execute(new ItemsUpdateTask(itemRepository,category,latch));
            }
            LOG.info("Задачи запущены, ожидаем завершения выполнения...");
            latch.await();
            LOG.info("Задачи выполнены, следующая порция...");
            chunk = categoryRepository.getChunk(pageRequest);
        }
        executorService.shutdown();
    }
}
