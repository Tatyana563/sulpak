package com.example.sulpak.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table
public class InfoItemValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String value;

    @ManyToOne
    private InfoItem infoItem;
    @ManyToMany
    private Set<Item> item;

    public InfoItemValue() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public InfoItem getInfoItem() {
        return infoItem;
    }

    public void setInfoItem(InfoItem infoItem) {
        this.infoItem = infoItem;
    }

    public Set<Item> getItem() {
        return item;
    }

    public void setItem(Set<Item> item) {
        this.item = item;
    }
}
