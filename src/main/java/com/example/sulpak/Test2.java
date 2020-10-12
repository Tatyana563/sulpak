package com.example.sulpak;

import com.example.sulpak.model.Item;
import com.example.sulpak.repository.ItemRepository;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class Test2 {
    private static final Logger LOG = LoggerFactory.getLogger(Test2.class);
   @Autowired
    private final ItemRepository itemRepository;

    public Test2(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }


    public static void main(String[] args) throws IOException {

        Document document = Jsoup.connect(" https://www.sulpak.kz/f/naushniki").get();


        Elements itemElements = document.select(".goods-container .tile-container");
        for (Element itemElement : itemElements) {
            Double itemPrice = Double.valueOf(itemElement.attr("data-price"));
            Element a = itemElement.selectFirst("a.title");
            String itemText = a.text();
            String itemLink = a.absUrl("href");

            LOG.info("Нашли товар {}/{}", itemText, itemPrice);

            String itemAvailability = itemElement.selectFirst("span.availability").text();
            Integer itemCode = Integer.valueOf(itemElement.attr("data-code"));
            String itemPhoto = itemElement.selectFirst(".goods-photo img").absUrl("src");

//            Item item = itemRepository.findOneByCode(itemCode).orElseGet(() -> new Item(itemCode));
//            item.setModel(itemText);
//            item.setPrice(itemPrice);
//            item.setImage(itemPhoto);
//            item.setUrl(itemLink);
//
//            item.setAvailable(StringUtils.containsIgnoreCase(itemAvailability, "есть в наличии")
//                    || StringUtils.containsIgnoreCase(itemAvailability, "товар на витрине"));
//
//            item.setCategory(category);
//            itemRepository.save(item);
        }
    }
}
