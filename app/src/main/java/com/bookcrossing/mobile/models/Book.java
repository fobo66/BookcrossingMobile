package com.bookcrossing.mobile.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties public class Book {
  private String author;
  private String name;
  private String description;
  private boolean free;
  private String positionName;
  private Coordinates position;
  private String city;
  private Date wentFreeAt;

  public Coordinates getPosition() {
    return position;
  }

  public void setPosition(Coordinates position) {
    this.position = position;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

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

  public String getPositionName() {
    return positionName;
  }

  public void setPositionName(String positionName) {
    this.positionName = positionName;
  }

  public Date getWentFreeAt() {
    return wentFreeAt;
  }

  public void setWentFreeAt(Date wentFreeAt) {
    this.wentFreeAt = wentFreeAt;
  }

  public Book(String author, String name, String description, boolean free, String positionName,
      Date wentFreeAt) {
    this.author = author;
    this.name = name;
    this.description = description;
    this.free = free;
    this.positionName = positionName;
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
