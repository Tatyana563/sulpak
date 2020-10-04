package com.example.sulpak;

import com.example.sulpak.model.Category;
import com.example.sulpak.model.Item;
import com.example.sulpak.repository.ItemRepository;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;


public class ItemsUpdateTask implements Runnable {
    private static final String URL = "https://www.sulpak.kz";
    private static final Logger LOG = LoggerFactory.getLogger(ItemsUpdateTask.class);

    private final ItemRepository itemRepository;
    private final Category category;
    private final CountDownLatch latch;

    private static final String CATEGORY_URL_REPLACEMENT = "https://www.sulpak.kz/f/";
    private static final String PAGE_URL_START = "https://www.sulpak.kz/filteredgoods/";
    private static final String PAGE_URL_NUMBER_FORMAT = "/~/~/NoveltyDesc/default/~/%d/31";

    public ItemsUpdateTask(ItemRepository itemRepository, Category category, CountDownLatch latch) {
        this.itemRepository = itemRepository;
        this.category = category;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            String categoryUrl = category.getUrl();
            String pageUrlFormat = categoryUrl.replace(CATEGORY_URL_REPLACEMENT, PAGE_URL_START) + PAGE_URL_NUMBER_FORMAT;
            String firstPageUrl = String.format(pageUrlFormat, 1);

            Document firstPage = Jsoup.connect(firstPageUrl).get();
            int totalPages = getTotalPages(firstPage);
            parseItems(firstPage);
            for (int i = 2; i <= totalPages; i++) {
                LOG.info("Получаем список товаров - страница {}", i);
                parseItems(Jsoup.connect(String.format(pageUrlFormat, i)).get());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }

    private int getTotalPages(Document firstPage) {
        Elements lastPage = firstPage.select(".pagination .pages-list a");
        if (!lastPage.isEmpty()) {
            return Integer.parseInt(lastPage.last().text());
        }
        return 0;
    }

    private void parseItems(Document page) {

        Elements itemElements = page.select(".goods-container .tile-container");
        for (Element itemElement : itemElements) {
            Double itemPrice = Double.valueOf(itemElement.attr("data-price"));
            Element a = itemElement.selectFirst("a.title");
            String itemText = a.text();
            String itemLink = a.absUrl("href");

            LOG.info("Нашли товар {}/{}", itemText, itemPrice);

            String itemAvailability = itemElement.selectFirst("span.availability").text();
            Integer itemCode = Integer.valueOf(itemElement.attr("data-code"));
            String itemPhoto = itemElement.selectFirst(".goods-photo img").absUrl("src");

            Item item = itemRepository.findOneByCode(itemCode).orElseGet(() -> new Item(itemCode));
            item.setModel(itemText);
            item.setPrice(itemPrice);
            item.setImage(itemPhoto);
            item.setUrl(itemLink);

            item.setAvailable(StringUtils.containsIgnoreCase(itemAvailability, "есть в наличии")
                    || StringUtils.containsIgnoreCase(itemAvailability, "товар на витрине"));

            item.setCategory(category);
            itemRepository.save(item);
        }
    }
}



