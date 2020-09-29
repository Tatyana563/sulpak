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

    private static final String PAGE_PARAM_FORMAT = "https://www.sulpak.kz/filteredgoods/akkumulyatoriy_k_telefonam/~/~/NoveltyDesc/default/~/1/%d";

    public ItemsUpdateTask(ItemRepository itemRepository, Category category, CountDownLatch latch) {
        this.itemRepository = itemRepository;
        this.category = category;
        this.latch = latch;
    }

    @Override
    public void run() {
        String categoryUrl = category.getUrl();
        String filterURL = categoryUrl.replace(" www.sulpak.kz/f/"," www.sulpak.kz/filteredgoods/");
        Document itemsPage = null;
        try {

            for (int i = 0; i < numberOfPages; i++) {
                LOG.info("Получаем список товаров - страница {}", i);
                Document newsPage = Jsoup.connect(String.format(PAGE_PARAM_FORMAT, i)).get();

            }

            itemsPage = Jsoup.connect(filterURL).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements itemElements = itemsPage.select(".goods-tiles");
        try {
            for (Element itemElement : itemElements) {
                String itemText = itemElement.select("a").text();
                String itemLink = URL + itemElement.select("a").attr("href");
                String itemPrice = (itemElement.select(".price").text());
                String itemAvailability = itemElement.select(".availability").text();
                String itemCode = itemElement.select(".code").text();
                String itemPhoto = itemElement.select(".goods-photo img").attr("src");
                Item item = new Item(itemText, itemPrice, itemCode, itemPhoto, itemLink);
                category.setPostProcessed(true);
                if (StringUtils.containsIgnoreCase(itemAvailability, "есть в наличии")
                        ||StringUtils.containsIgnoreCase(itemAvailability, "товар на витрине")) {
                    item.setAvailable(true);
                } else {
                    item.setAvailable(false);
                }
                if (!itemRepository.existsByCode(itemCode)) {
                    itemRepository.save(item);
                }
            }
        } finally {
            latch.countDown();
        }
    }
}



