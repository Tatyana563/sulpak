package com.example.sulpak.model;

import javax.persistence.*;

@Entity
@Table
public class InfoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String key;

    @ManyToOne
    private InfoGroup infoGroup;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public InfoGroup getInfoGroup() {
        return infoGroup;
    }

    public void setInfoGroup(InfoGroup infoGroup) {
        this.infoGroup = infoGroup;
    }

}
