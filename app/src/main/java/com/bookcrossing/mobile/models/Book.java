package com.bookcrossing.mobile.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Book {
    private String author;
    private String name;
    private String description;
    private boolean free;
    private String position;
    private Date wentFreeAt;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Date getWentFreeAt() {
        return wentFreeAt;
    }

    public void setWentFreeAt(Date wentFreeAt) {
        this.wentFreeAt = wentFreeAt;
    }

    public Book(String author, String name, String description, boolean free, String position, Date wentFreeAt) {
        this.author = author;
        this.name = name;
        this.description = description;
        this.free = free;
        this.position = position;
        this.wentFreeAt = wentFreeAt;
    }

    public Book() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
