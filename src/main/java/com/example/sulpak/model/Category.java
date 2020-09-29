package com.example.sulpak.model;

import javax.persistence.*;

@Entity
@Table
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String url;
    private Boolean postProcessed;
    @ManyToOne
    private MainGroup group;

    public Category() {
    }

    public Category(String name, String url, Boolean postProcessed, MainGroup group) {
        this.name = name;
        this.url = url;
        this.postProcessed = postProcessed;
        this.group = group;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String categoryName) {
        this.name = categoryName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String categoryURL) {
        this.url = categoryURL;
    }

    public MainGroup getGroup() {
        return group;
    }

    public void setGroup(MainGroup group) {
        this.group = group;
    }

    public Boolean getPostProcessed() {
        return postProcessed;
    }

    public void setPostProcessed(Boolean postProcessed) {
        this.postProcessed = postProcessed;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", categoryName='" + name + '\'' +
                ", categoryURL='" + url + '\'' +
                '}';
    }
}
