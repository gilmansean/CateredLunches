package com.example.cateredlunches.model;

public class MenuDay {
    private String dayOfTheWeek;
    private String menuItem;

    public MenuDay() {
    }

    public MenuDay(String dayOfTheWeek, String menuItem) {
        this.dayOfTheWeek = dayOfTheWeek;
        this.menuItem = menuItem;
    }

    public String getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public void setDayOfTheWeek(String dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public String getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(String menuItem) {
        this.menuItem = menuItem;
    }
}
