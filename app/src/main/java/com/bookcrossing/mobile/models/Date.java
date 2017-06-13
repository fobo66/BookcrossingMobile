package com.bookcrossing.mobile.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties public class Date {

  private Integer year;
  private Integer month;
  private Integer day;
  private Long timestamp;

  public Date() {
  }

  public Date(int year, int month, int day, Long timestamp) {
    this.year = year;
    this.month = month;
    this.day = day;
    this.timestamp = timestamp;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public int getDay() {
    return day;
  }

  public void setDay(int day) {
    this.day = day;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
}
