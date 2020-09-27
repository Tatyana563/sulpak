package com.example.sulpak;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect("https://www.sulpak.kz/p/telefoniy_i_gadzhetiy").get();
        Elements select = document.select(".portal-menu-title");
    }
}
